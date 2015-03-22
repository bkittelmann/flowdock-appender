package com.bitmoving.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import org.slf4j.Marker;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MessageBuilder {
    // set through configuration
    private final String flowToken;
    private final String author;
    private final PatternLayoutEncoder encoder;

    // used only within
    private final Map<Level, String> statusColors = new HashMap<Level, String>();

    public MessageBuilder(String flowToken, String author, PatternLayoutEncoder encoder) {
        this.flowToken = flowToken;
        this.author = author;
        this.encoder = encoder;
        initStatusColor();
    }

    public String build(ILoggingEvent event) throws IOException {
        event.getFormattedMessage();
        JsonObject json = buildMessage(event);
        return serialize(json);
    }

    private JsonObject buildMessage(ILoggingEvent event) throws IOException {
        JsonObject message = new JsonObject();
        message.add("flow_token", flowToken);
        message.add("event", "activity");

        message.add("author", buildAuthor());
        message.add("title", event.getLoggerName());
        message.add("external_thread_id", buildExternalThreadId());

        return message.add("thread", buildThread(event));
    }

    private String buildExternalThreadId() {
        return UUID.randomUUID().toString();
    }

    private JsonObject buildAuthor() {
        return new JsonObject().add("name", author);
    }

    private JsonObject buildThread(ILoggingEvent event) {
        JsonObject thread = new JsonObject();
        thread.add("title", event.getFormattedMessage());
        thread.add("fields", buildFields(event));
        thread.add("body", "<pre>" + encoder.getLayout().doLayout(event) + "</pre>");
        thread.add("external_url", "http://example.org/test");
        buildStatus(event, thread);
        return thread;
    }

    private JsonArray buildFields(ILoggingEvent event) {
        JsonArray fields = new JsonArray();
        addMarker(event, fields);
        return fields;
    }

    private void addMarker(ILoggingEvent event, JsonArray fields) {
        JsonObject field = new JsonObject();
        Marker marker = event.getMarker();
        if (marker != null) {
            field.add("label", "Marker");
            field.add("value", marker.getName());
            fields.add(field);
        }
    }

    private void buildStatus(ILoggingEvent event, JsonObject parent) {
        if (statusColors.containsKey(event.getLevel())) {
            JsonObject status = new JsonObject();
            status.add("color", statusColors.get(event.getLevel()));
            status.add("value", event.getLevel().toString().toLowerCase(Locale.ENGLISH));
            parent.add("status", status);
        }
    }

    private void initStatusColor() {
        statusColors.put(Level.ERROR, "red");
        statusColors.put(Level.WARN, "orange");
        statusColors.put(Level.INFO, "yellow");
    }

    private String serialize(JsonObject json) throws IOException {
        StringWriter writer = new StringWriter();
        json.writeTo(writer);
        writer.close();
        return writer.getBuffer().toString();
    }
}