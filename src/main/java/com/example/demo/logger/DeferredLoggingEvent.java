package com.example.demo.logger;

import ch.qos.logback.core.spi.DeferredProcessingAware;
import org.slf4j.Logger;
import org.slf4j.event.DefaultLoggingEvent;
import org.slf4j.event.Level;

public class DeferredLoggingEvent extends DefaultLoggingEvent implements DeferredProcessingAware {
    public DeferredLoggingEvent(Level level, Logger logger) {
        super(level, logger);
    }

    @Override
    public void prepareForDeferredProcessing() {

    }
}
