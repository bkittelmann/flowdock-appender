package com.bitmoving.util;


import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;

import java.io.IOException;

public class FlowdockAppender extends AppenderBase<ILoggingEvent> {
    public static final String DEFAULT_ENDPOINT = "https://api.flowdock.com/messages";

    // not configurable from logback config
    private MessageSender sender;
    private MessageBuilder builder;

    // configurable through logback config
    private LayoutWrappingEncoder encoder;
    private String flowToken;
    private String apiEndpoint = DEFAULT_ENDPOINT;
    private String author;

    @Override
    public void start() {
        if (this.encoder == null) {
            addError("No encoder set for appender name [" + name + "]");
            return;
        }

        try {
            encoder.init(System.out);
            sender = new MessageSender(apiEndpoint, this);
            builder = new MessageBuilder(flowToken, author, encoder);
        } catch (IOException e) {
            addError("Exception when initializing encoder", e);
        }

        super.start();
    }

    @Override
    public void stop() {
        sender.close();
        super.stop();
    }

    @Override
    protected void append(ILoggingEvent event) {
        try {
            String message = builder.build(event);
            sender.sendRequest(message);
        } catch (IOException e) {
            addError("Could not build message", e);
        }
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LayoutWrappingEncoder getEncoder() {
        return encoder;
    }

    public void setEncoder(LayoutWrappingEncoder encoder) {
        this.encoder = encoder;
    }

    public String getFlowToken() {
        return flowToken;
    }

    public void setFlowToken(String flowToken) {
        this.flowToken = flowToken;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }
}
