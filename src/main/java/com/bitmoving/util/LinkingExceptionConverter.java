package com.bitmoving.util;


import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;

public class LinkingExceptionConverter extends ThrowableHandlingConverter {

    private String base = "https://github.com/bkittelmann/flowdock-appender/tree/master/src/main/java/";

    private String rootPackage = "com.bitmoving";

    @Override
    public String convert(ILoggingEvent event) {
        IThrowableProxy itp = event.getThrowableProxy();
        if (itp instanceof ThrowableProxy) {
            System.out.println("In linking exception converter");
            ThrowableProxy tp = (ThrowableProxy)itp;

            StringBuilder builder = new StringBuilder();

            for (StackTraceElementProxy step: tp.getStackTraceElementProxyArray()) {
                String className = step.getStackTraceElement().getClassName();
                if (className.startsWith(rootPackage)) {
                    String fileName = step.getStackTraceElement().getFileName();
                    String methodName = step.getStackTraceElement().getMethodName();
                    int lineNumber = step.getStackTraceElement().getLineNumber();

                    String replacement = "." + fileName + "#L" + lineNumber;
                    String result = className.replace(".", "/"). replaceAll("/\\w+$", replacement);
                    builder.append(String.format("<br/>at %s.%s(<a href=\"%s\">%s</a>)", className, methodName, base + result, fileName + ":" + lineNumber));
                } else {
                    builder.append("<br/>" + step.getSTEAsString());
                }
            }
            return builder.toString();
        }
        return "";
    }
}
