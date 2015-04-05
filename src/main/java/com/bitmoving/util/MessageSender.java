package com.bitmoving.util;

import ch.qos.logback.core.spi.ContextAware;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;

import java.util.concurrent.ExecutionException;

public class MessageSender {
    private final String endpoint;
    private final ContextAware appender;
    private final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    public MessageSender(String endpoint, ContextAware appender) {
        this.endpoint = endpoint;
        this.appender = appender;
    }

    public void sendRequest(String json) {
        try {
            RequestBuilder requestBuilder = new RequestBuilder("POST");
            Request request = requestBuilder
                    .setUrl(endpoint)
                    .setBody(json)
                    .addHeader("Content-Type", "application/json")
                    .build();

            Response response = asyncHttpClient.executeRequest(request).get();
            int statusCode = response.getStatusCode();
            if (statusCode > 299) {
                appender.addError(String.format("Could not store in flowdock: %s %s", statusCode, response.getStatusText()));
            }
        } catch (InterruptedException e) {
            appender.addError("Exception while sending message", e);
        } catch (ExecutionException e) {
            appender.addError("Exception while sending message", e);
        }
    }

    public void close() {
        asyncHttpClient.close();
    }
}
