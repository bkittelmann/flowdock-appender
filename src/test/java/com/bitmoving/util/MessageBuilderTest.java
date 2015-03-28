package com.bitmoving.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.junit.Test;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class MessageBuilderTest {

    private final LoggerContext context = new LoggerContext();
    private final Logger logger = context.getLogger(MessageBuilderTest.class);
    private final LayoutWrappingEncoder encoder = setupEncoder();

    @Test
    public void basic_fields_are_set_on_message() throws IOException {
        String token = "token";
        String author = "author";
        MessageBuilder builder = new MessageBuilder(token, author, encoder);

        LoggingEvent event = new LoggingEvent("", logger, Level.DEBUG, "a log message", null, null);
        JsonObject message = builder.buildMessage(event);

        assertThat(message.get("flow_token").asString(), equalTo(token));
        assertThat(message.get("author").asObject().get("name").asString(), equalTo(author));
    }

    @Test
    public void put_marker_values_into_fields() throws IOException {
        Marker marker = MarkerFactory.getMarker("COUNTRY");
        LoggingEvent event = new LoggingEvent("", logger, Level.ALL, "a log message", null, null);
        event.setMarker(marker);

        MessageBuilder builder = new MessageBuilder("token", "author", encoder);

        JsonObject message = builder.buildMessage(event);
        JsonValue fields = message.get("thread").asObject().get("fields");

        assertThat(fields.asArray().size(), equalTo(1));

        JsonObject field = fields.asArray().get(0).asObject();

        assertThat(field.get("label").asString(), equalTo("Marker"));
        assertThat(field.get("value").asString(), equalTo(marker.getName()));
    }

    @Test
    public void convert_log_level_to_status() throws IOException {
        LoggingEvent event = new LoggingEvent("", logger, Level.ERROR, "", null, null);
        MessageBuilder builder = new MessageBuilder("token", "author", encoder);

        JsonObject message = builder.buildMessage(event);
        JsonObject status = message.get("thread").asObject().get("status").asObject();

        assertThat(status.get("value").asString(), equalTo("error"));
        assertThat(status.get("color").asString(), equalTo("red"));
    }

    private LayoutWrappingEncoder setupEncoder() {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("[%thread] %-5level %logger{35} - %msg %n");
        encoder.start();
        return encoder;
    }
}
