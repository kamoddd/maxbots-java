package io.kimconsulting.maxbots.core.context;

@FunctionalInterface
public interface MiddlewareChain {
    void proceed(MaxContext context) throws Exception;
}
