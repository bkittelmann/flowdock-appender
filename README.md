# flowdock-appender

[![Build Status](https://travis-ci.org/bkittelmann/flowdock-appender.svg?branch=master)]
(https://travis-ci.org/bkittelmann/flowdock-appender) [![Coverage Status](https://coveralls.io/repos/bkittelmann/flowdock-appender/badge.svg)](https://coveralls.io/r/bkittelmann/flowdock-appender) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.bitmoving.util/flowdock-appender/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.bitmoving.util/flowdock-appender)

Logback appender that sends messages to into a Flowdock flow.

## Installation

Maven coordinates for latest released version:

```xml
<dependency>
    <groupId>com.bitmoving.util</groupId>
    <artifactId>flowdock-appender</artifactId>
    <version>0.1.2</version>
</dependency>

```

## Example

Show complete logback file:

```xml
<configuration debug="true">
    <appender name="FLOWDOCK" class="com.bitmoving.util.FlowdockAppender">
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>%msg%n</pattern>
        </encoder>
        <flowToken>here-goes-your-flowdock-token</flowToken>
    </appender>

    <root level="debug">
        <appender-ref ref="FLOWDOCK"/>
    </root>
</configuration>
```

## Configuration

Appender configuration


## Integrate with Flowdock

Use the script to get a token id
Set token id on
