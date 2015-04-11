package com.bitmoving.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.spi.ContextAware;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
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
    private final LayoutWrappingEncoder encoder;
    private final int maxTitleChars;

    // used only within
    private final Map<Level, String> statusColors = new HashMap<Level, String>();

    public MessageBuilder(String flowToken, String author, LayoutWrappingEncoder encoder, int maxTitleChars) {
        this.flowToken = flowToken;
        this.author = author;
        this.encoder = encoder;
        this.maxTitleChars = maxTitleChars;
        initStatusColor();
    }

    public String build(ILoggingEvent event) {
        JsonObject json = buildMessage(event);
        return json.toString();
    }

    public JsonObject buildMessage(ILoggingEvent event) {
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
        thread.add("title", buildThreadTitle(event));
        thread.add("fields", buildFields(event));
        thread.add("body", encoder.getLayout().doLayout(event));
        thread.add("external_url", "http://example.org/test");
        buildStatus(event, thread);
        return thread;
    }

    private String buildThreadTitle(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        if (message.length() > maxTitleChars) {
            return event.getFormattedMessage().substring(0, maxTitleChars) + "[..]";
        }
        return event.getFormattedMessage();
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
        statusColors.put(Level.DEBUG, "grey");
        statusColors.put(Level.TRACE, "grey");
    }
}