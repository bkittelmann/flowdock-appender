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


## Installation to Flowdock

The log messages arrive in Flowdock through an **Integration**. There is a general [Integration guide](https://www.flowdock.com/api/integration-guide)
available which explains the concepts behind it. What you need to obtain is a *flow token*. It's easiest done by 
executing their interactive helper script [flowdock-oauth.rb](https://raw.githubusercontent.com/flowdock/flowdock-example-integration/master/flowdock-oauth.rb).

At the end of that script, you should be able to select the flows you want to create flow tokens for.
