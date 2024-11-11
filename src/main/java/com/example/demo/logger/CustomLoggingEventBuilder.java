package com.example.demo.logger;

import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.slf4j.spi.DefaultLoggingEventBuilder;
import org.slf4j.spi.LoggingEventBuilder;

import java.util.function.Supplier;

public class CustomLoggingEventBuilder extends DefaultLoggingEventBuilder {

    public CustomLoggingEventBuilder(Logger logger, Level level) {
        super(logger, level);
    }


    @Override
    public LoggingEventBuilder addArgument(Supplier<?> objectSupplier) {
        loggingEvent.addArgument(objectSupplier);
        return this;
    }

    @Override
    public LoggingEventBuilder setMessage(Supplier<String> messageSupplier) {
        loggingEvent.addArgument(messageSupplier);
        return this;
    }

    @Override
    public void log(Supplier<String> messageSupplier) {
       loggingEvent.addArgument(messageSupplier);
       log(loggingEvent);
    }
}
