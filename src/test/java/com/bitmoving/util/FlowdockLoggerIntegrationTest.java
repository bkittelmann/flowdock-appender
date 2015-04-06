package com.bitmoving.util;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusListenerAsList;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class FlowdockLoggerIntegrationTest {

    private static final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

    @Test
    public void status_stored_on_error_while_sending() {
        LoggerContext context = new LoggerContext();
        StatusListenerAsList statusListener = trackStatusWithListener(context);

        FlowdockAppender appender = new FlowdockAppender();
        appender.setApiEndpoint("http://localhost:7800");
        appender.setFlowToken("token");
        appender.setAuthor("author");
        appender.setEncoder(createEncoder(context));
        appender.setName("FLOWDOCK");
        appender.setContext(context);
        appender.start();

        Logger logger = loggerWithAppender(appender);
        logger.error("message", new RuntimeException("ouch"));

        List<Status> statusList = statusListener.getStatusList();
        assertThat(statusList.size(), equalTo(1));

        Status status = statusList.get(0);
        assertTrue(status.getOrigin().getClass() == FlowdockAppender.class);
        assertThat(status.getMessage(), containsString("Could not send message"));
    }

    @Test
    public void do_not_start_appender_if_author_is_missing() {
        LoggerContext context = new LoggerContext();
        StatusListenerAsList statusListener = trackStatusWithListener(context);

        FlowdockAppender appender = new FlowdockAppender();
        appender.setEncoder(createEncoder(context));
        appender.setName("FLOWDOCK");
        appender.setContext(context);
        appender.start();

        assertThat(appender.isStarted(), is(false));

        Logger logger = loggerWithAppender(appender);
        logger.error("message");

        List < Status > statusList = statusListener.getStatusList();
        assertThat(statusList.size(), equalTo(2));

        Status status = statusList.get(0);
        assertTrue(status.getOrigin().getClass() == FlowdockAppender.class);
        assertThat(status.getMessage(), containsString("No author set"));
    }

    private FlowdockAppender startAppender(String endpoint, LoggerContext context) {
        FlowdockAppender appender = new FlowdockAppender();
        appender.setApiEndpoint(endpoint);
        appender.setFlowToken("token");
        appender.setAuthor("author");
        appender.setEncoder(createEncoder(context));
        appender.setName("FLOWDOCK");
        appender.setContext(context);
        appender.start();
        return appender;
    }

    private Logger loggerWithAppender(Appender<ILoggingEvent> appender) {
        Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("test1");
        logger.addAppender(appender);
        logger.setLevel(Level.DEBUG);
        logger.setAdditive(false);
        return logger;
    }

    private StatusListenerAsList trackStatusWithListener(LoggerContext context) {
        StatusListenerAsList statusListener = new StatusListenerAsList();
        context.getStatusManager().add(statusListener);
        return statusListener;
    }

    private LayoutWrappingEncoder createEncoder(LoggerContext context) {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("[%thread] %-5level %logger{40} - %msg %n");
        encoder.start();
        return encoder;
    }
}
