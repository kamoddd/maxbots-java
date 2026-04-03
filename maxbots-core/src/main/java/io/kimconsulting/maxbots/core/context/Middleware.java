package io.kimconsulting.maxbots.core.context;

@FunctionalInterface
public interface Middleware {
    void handle(MaxContext context, MiddlewareChain next) throws Exception;
}
