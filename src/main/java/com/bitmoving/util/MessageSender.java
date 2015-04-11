package com.bitmoving.util;

import ch.qos.logback.core.spi.ContextAware;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MessageSender {
    private final URL endpoint;
    private final ContextAware appender;
    private ExecutorService executor =  Executors.newFixedThreadPool(10);

    public MessageSender(URL endpoint, ContextAware appender) {
        this.endpoint = endpoint;
        this.appender = appender;
    }

    public void sendRequest(String json) {
        RequestRunnable runnable = new RequestRunnable(endpoint, json, appender);
        executor.execute(runnable);
    }

    public void stop() {
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            appender.addError("Problem waiting for request execution to finish", e);
        }
    }

    public void setExecutorService(ExecutorService executorService) {
        executor = executorService;
    }
}

