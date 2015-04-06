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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class FlowdockLoggerIntegrationTest {

    // doing this to silence the SLF4J warning that a logger will be substituted during init phase
    private static final OutputStream defaultErr = System.err;
    private static final ByteArrayOutputStream myOut = new ByteArrayOutputStream();

    private static final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

    @BeforeClass
    public static void init() {
        System.setErr(new PrintStream(myOut));
        context.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.ERROR);
    }

    @Test
    public void status_stored_on_error_while_sending() {
        StatusListenerAsList statusListener = trackStatusWithListener();
        FlowdockAppender appender = startAppender("http://localhost:7800");
        Logger logger = loggerWithAppender(appender);
        logger.error("message", new RuntimeException("ouch"));

        List<Status> statusList = statusListener.getStatusList();
        assertThat(statusList.size(), equalTo(1));

        Status status = statusList.get(0);
        assertTrue(status.getOrigin().getClass() == FlowdockAppender.class);
        assertThat(status.getMessage(), containsString("Exception while sending message"));
        appender.stop();
    }

    @AfterClass
    public static void unset() throws IOException {
        myOut.close();
        System.setErr(new PrintStream(defaultErr));
    }

    private FlowdockAppender startAppender(String endpoint) {
        FlowdockAppender appender = new FlowdockAppender();
        appender.setApiEndpoint(endpoint);
        appender.setFlowToken("token");
        appender.setAuthor("author");
        appender.setEncoder(createEncoder());
        appender.setMaxTitleChars(40);
        appender.setName("FLOWDOCK");
        appender.setContext(context);
        appender.start();
        return appender;
    }

    private Logger loggerWithAppender(Appender<ILoggingEvent> appender) {
        Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(getClass());
        logger.addAppender(appender);
        logger.setLevel(Level.DEBUG);
        logger.setAdditive(false);
        return logger;
    }

    private StatusListenerAsList trackStatusWithListener() {
        StatusListenerAsList statusListener = new StatusListenerAsList();
        context.getStatusManager().add(statusListener);
        return statusListener;
    }

    private LayoutWrappingEncoder createEncoder() {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("[%thread] %-5level %logger{40} - %msg %n");
        encoder.start();
        return encoder;
    }
}
