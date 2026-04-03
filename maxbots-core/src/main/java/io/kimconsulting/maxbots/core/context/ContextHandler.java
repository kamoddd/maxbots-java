package io.kimconsulting.maxbots.core.context;

@FunctionalInterface
public interface ContextHandler {
    void handle(MaxContext context) throws Exception;
}
