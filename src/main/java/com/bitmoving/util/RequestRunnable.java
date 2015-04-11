package com.bitmoving.util;

import ch.qos.logback.core.spi.ContextAware;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

class RequestRunnable implements Runnable {

    private final URL endpoint;
    private final String json;
    private final ContextAware appender;
    private final Charset charset = Charset.forName("UTF-8");

    public RequestRunnable(URL endpoint, String json, ContextAware appender) {
        this.endpoint = endpoint;
        this.json = json;
        this.appender = appender;
    }

    @Override
    public void run() {
        try {
            HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection();

            connection.setRequestMethod("POST");
            connection.addRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            OutputStream output = connection.getOutputStream();
            output.write(json.getBytes(charset));
            output.close();

            int statusCode = connection.getResponseCode();
            if (statusCode > 299) {
                appender.addError(String.format("Could not store in Flowdock: %s %s", statusCode, connection.getResponseMessage()));
            }
            connection.disconnect();
        } catch (IOException e) {
            appender.addError("Could not send message", e);
        }
    }
}
