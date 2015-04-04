package com.bitmoving.util;


import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.util.StatusPrinter;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class FlowdockAppender extends AppenderBase<ILoggingEvent> {
    public static final String DEFAULT_ENDPOINT = "https://api.flowdock.com/messages";

    // not configurable from logback config
    final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
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

    private String buildJson(ILoggingEvent event) throws IOException {
        return new MessageBuilder(flowToken, author, encoder).build(event);
    }

    protected void sendRequest(ILoggingEvent event) {
        try {
            String json = buildJson(event);

            RequestBuilder requestBuilder = new RequestBuilder("POST");
            Request request = requestBuilder
                .setUrl(apiEndpoint)
                .setBody(json)
                .addHeader("Content-Type", "application/json")
                .build();

            Response response = asyncHttpClient.executeRequest(request).get();
            int statusCode = response.getStatusCode();
            if (statusCode > 299) {
                addError(String.format("Could not store in flowdock: %s %s", statusCode, response.getStatusText()));
            }
        } catch (IOException e) {
            addError("Could not send request to Flowdock", e);
        } catch (InterruptedException e) {
            addError("Exception while sending message", e);
        } catch (ExecutionException e) {
            addError("Exception while sending message", e);
        }
    }
}
