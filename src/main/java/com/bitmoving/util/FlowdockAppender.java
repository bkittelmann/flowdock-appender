package com.bitmoving.util;


import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;

import java.io.IOException;

public class FlowdockAppender extends AppenderBase<ILoggingEvent> {
    // configurable through logback config
    private PatternLayoutEncoder encoder;
    private String flowToken;
    private String author;

    // not configurable from logback config
    final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    @Override
    public void start() {
        if (this.encoder == null) {
            addError("No encoder set for appender name [" + name + "]");
            return;
        }

        try {
            encoder.init(System.out);
        } catch (IOException e) {
            addError("Exception when initializing encoder", e);
        }

        super.start();
    }

    @Override
    public void stop() {
        asyncHttpClient.close();
        super.stop();
    }

    @Override
    protected void append(ILoggingEvent event) {
        sendRequest(event);
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public PatternLayoutEncoder getEncoder() {
        return encoder;
    }

    public void setEncoder(PatternLayoutEncoder encoder) {
        this.encoder = encoder;
    }

    public String getFlowToken() {
        return flowToken;
    }

    public void setFlowToken(String flowToken) {
        this.flowToken = flowToken;
    }

    private String buildJson(ILoggingEvent event) throws IOException {
        return new MessageBuilder(flowToken, author, encoder).build(event);
    }

    protected void sendRequest(ILoggingEvent event) {
        final String url = "https://api.flowdock.com/messages";

        try {
            String json = buildJson(event);
            RequestBuilder requestBuilder = new RequestBuilder("POST");
            Request request = requestBuilder
                    .setUrl(url)
                    .setBody(json)
                    .addHeader("Content-Type", "application/json")
                    .build();

            asyncHttpClient.executeRequest(request);
        } catch (IOException e) {
            addError("Could not send request to Flowdock", e);
        }
    }
}
