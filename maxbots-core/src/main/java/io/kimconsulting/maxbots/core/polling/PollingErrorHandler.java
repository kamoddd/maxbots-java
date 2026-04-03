package io.kimconsulting.maxbots.core.polling;

@FunctionalInterface
public interface PollingErrorHandler {
    void handle(Throwable error, Long marker);
}
