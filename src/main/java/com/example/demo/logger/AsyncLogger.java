package com.example.demo.logger;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.AbstractLogger;
import org.slf4j.spi.LoggingEventBuilder;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

/**
 * <p>
 * AsyncLogger separates out the logging calls into another thread. Async logging thread can take methods to be execute
 * is separate thread for logging purposes, like the heavy operation of masking data
 * </p>
 *
 * <p>Refer `logging-utils/README.md` for usage.</p>
 */
@Slf4j
public class AsyncLogger extends AbstractLogger {
    private static final BlockingQueue<Supplier<Void>> logQueue;
    private static final Thread logProcessingThread;
    private static final String THREAD_NAME = AsyncLogger.class.getSimpleName() + "-Thread";
    private final Level currentLevel;
    private final Logger slf4jLogger;

    static {
        logQueue = new LinkedBlockingQueue<>();
        logProcessingThread = new Thread(AsyncLogger::processLog, THREAD_NAME);

        // In case of application shutdown, wait for the logger thread to complete the queue tasks.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Waiting for LoggingThread Shutdown... ");
            try {
                logProcessingThread.interrupt();
                logProcessingThread.join();
            } catch (InterruptedException e) {
                log.error("Error in joining the logger thread", e);
            }
        }));
    }

    public AsyncLogger(String name) {
        this.slf4jLogger = LoggerFactory.getLogger(name);
        if (!logProcessingThread.isAlive())
            logProcessingThread.start();
        this.currentLevel = getDefaultLogLevel();
    }



    public AsyncLogger(Class<?> clazz) {
        this.slf4jLogger = LoggerFactory.getILoggerFactory().getLogger(clazz.getName());
        if (!logProcessingThread.isAlive())
            logProcessingThread.start();
        this.currentLevel = getDefaultLogLevel();
    }

    private Level getDefaultLogLevel() {
        try {
            Properties properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream("application.properties"));
            String level = properties.getProperty("logging.level", "INFO").toUpperCase();
            return Level.valueOf(level);
        } catch (Exception e) {
            return Level.INFO;
        }
    }

    private static void processLog() {
        while (!Thread.currentThread().isInterrupted()) {
            Supplier<Void> logger;
            try {
                logger = logQueue.take();
                logger.get();
            } catch (InterruptedException ie) {
                log.info("InterruptedException in AsyncLogging thread, performing drainQueue operation.", ie);
                drainQueue();
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Error in AsyncLogging thread", e);
            }
        }
    }

    private static void drainQueue() {
        while (!logQueue.isEmpty()) {
            try {
                logQueue.take().get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public LoggingEventBuilder makeLoggingEventBuilder(Level level) {
        return new CustomLoggingEventBuilder(this, level);
    }


    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker, String messagePattern, Object[] arguments, Throwable throwable) {

        switch (level) {
            case DEBUG:
                if (slf4jLogger.isDebugEnabled()) {
                    logQueue.add(() -> {
                        preprocess(arguments);
                        slf4jLogger.debug(messagePattern, combineArgs(arguments, throwable));
                        return null;
                    });
                }
                break;
            case INFO:
                if (slf4jLogger.isInfoEnabled()) {
                    logQueue.add(() -> {
                        preprocess(arguments);
                        slf4jLogger.info(messagePattern, combineArgs(arguments, throwable));
                        return null;
                    });
                }
                break;
            case WARN:
                if (slf4jLogger.isWarnEnabled()) {
                    logQueue.add(() -> {
                        preprocess(arguments);
                        slf4jLogger.warn(messagePattern, combineArgs(arguments, throwable));
                        return null;
                    });
                }
                break;
            case ERROR:
                if (slf4jLogger.isErrorEnabled()) {
                    logQueue.add(() -> {
                        preprocess(arguments);
                        slf4jLogger.error(messagePattern, combineArgs(arguments, throwable));
                        return null;
                    });
                }
                break;
            default:
                if (slf4jLogger.isInfoEnabled()) {
                    logQueue.add(() -> {
                        preprocess(arguments);
                        slf4jLogger.info(messagePattern, combineArgs(arguments, throwable));
                        return null;
                    });
                }
                break;
        }
    }

    private Object[] combineArgs(Object[] arguments, Throwable t) {

        int argLen = arguments == null ? 0 : arguments.length;
        int tLen = t == null ? 0 : 1;
        Object[] combinedArguments = new Object[argLen + tLen];
        if (arguments != null) {
            System.arraycopy(arguments, 0, combinedArguments, 0, argLen);
        }
        if (t != null) {
            combinedArguments[argLen] = t;
        }
        return combinedArguments;
    }

    private void preprocess(Object[] arguments) {
        if(arguments != null)
            for (int i=0;i<arguments.length; ++i) {
                if (arguments[i] instanceof Supplier<?>) {
                    arguments[i] = ((Supplier<?>) arguments[i]).get();
                }
            }
    }

    @Override
    public boolean isTraceEnabled() {
        return currentLevel.toInt() <= Level.TRACE.toInt();
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return false;
    }

    @Override
    public boolean isDebugEnabled() {
        return currentLevel.toInt() <= Level.DEBUG.toInt();
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return false;
    }

    @Override
    public boolean isInfoEnabled() {
        return currentLevel.toInt() <= Level.INFO.toInt();
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return false;
    }

    @Override
    public boolean isWarnEnabled() {
        return currentLevel.toInt() <= Level.WARN.toInt();
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return false;
    }

    @Override
    public boolean isErrorEnabled() {
        return currentLevel.toInt() <= Level.ERROR.toInt();
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return false;
    }

    @Override
    protected String getFullyQualifiedCallerName() {
        return null;
    }

}