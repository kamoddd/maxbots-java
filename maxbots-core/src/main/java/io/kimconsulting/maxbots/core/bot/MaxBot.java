package io.kimconsulting.maxbots.core.bot;

import io.kimconsulting.maxbots.api.http.MaxApi;
import io.kimconsulting.maxbots.api.http.MaxApiClient;
import io.kimconsulting.maxbots.api.model.Message;
import io.kimconsulting.maxbots.api.model.Update;
import io.kimconsulting.maxbots.api.model.UpdateType;
import io.kimconsulting.maxbots.core.context.ContextHandler;
import io.kimconsulting.maxbots.core.context.MaxContext;
import io.kimconsulting.maxbots.core.context.Middleware;
import io.kimconsulting.maxbots.core.context.MiddlewareChain;
import io.kimconsulting.maxbots.core.dispatch.PatternHandler;
import io.kimconsulting.maxbots.core.polling.LongPollingBotRunner;
import io.kimconsulting.maxbots.core.polling.PollingOptions;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MaxBot {
    private final MaxApi api;
    private final List<Middleware> middlewares = new CopyOnWriteArrayList<>();
    private final List<ContextHandler> anyHandlers = new CopyOnWriteArrayList<>();
    private final Map<UpdateType, List<ContextHandler>> handlersByType = new EnumMap<>(UpdateType.class);
    private final List<PatternHandler> hearsHandlers = new CopyOnWriteArrayList<>();
    private final List<PatternHandler> actionHandlers = new CopyOnWriteArrayList<>();
    private final List<PatternHandler> commandHandlers = new CopyOnWriteArrayList<>();
    private volatile PollingOptions pollingOptions = PollingOptions.builder().build();
    private volatile LongPollingBotRunner runner;

    public MaxBot(MaxApi api) {
        this.api = Objects.requireNonNull(api, "api");
    }

    public static MaxBot withToken(String token) {
        return new MaxBot(new MaxApiClient(token));
    }

    public MaxApi api() {
        return api;
    }

    public MaxBot use(Middleware middleware) {
        middlewares.add(middleware);
        return this;
    }

    public MaxBot onAny(ContextHandler handler) {
        anyHandlers.add(handler);
        return this;
    }

    public MaxBot on(UpdateType updateType, ContextHandler handler) {
        handlersByType.computeIfAbsent(updateType, ignored -> new CopyOnWriteArrayList<>()).add(handler);
        return this;
    }

    public MaxBot command(String command, ContextHandler handler) {
        Pattern pattern = Pattern.compile("^/" + Pattern.quote(command) + "(?:@\\w+)?(?:\\s+(.*))?$", Pattern.CASE_INSENSITIVE);
        commandHandlers.add(new PatternHandler(pattern, handler));
        return this;
    }

    public MaxBot hears(String text, ContextHandler handler) {
        Pattern pattern = Pattern.compile("^" + Pattern.quote(text) + "$", Pattern.CASE_INSENSITIVE);
        hearsHandlers.add(new PatternHandler(pattern, handler));
        return this;
    }

    public MaxBot hears(Pattern pattern, ContextHandler handler) {
        hearsHandlers.add(new PatternHandler(pattern, handler));
        return this;
    }

    public MaxBot action(String payload, ContextHandler handler) {
        Pattern pattern = Pattern.compile("^" + Pattern.quote(payload) + "$");
        actionHandlers.add(new PatternHandler(pattern, handler));
        return this;
    }

    public MaxBot action(Pattern pattern, ContextHandler handler) {
        actionHandlers.add(new PatternHandler(pattern, handler));
        return this;
    }

    public MaxBot polling(PollingOptions options) {
        this.pollingOptions = options;
        return this;
    }

    public LongPollingBotRunner start() {
        LongPollingBotRunner current = new LongPollingBotRunner(api, this, pollingOptions);
        current.start();
        this.runner = current;
        return current;
    }

    public void stop() {
        LongPollingBotRunner current = runner;
        if (current != null) {
            current.stop();
        }
    }

    public void handle(Update update) {
        MaxContext context = new MaxContext(api, update);
        try {
            applyMiddlewares(context, buildTerminalHandlers(update, context), 0);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to process MAX update", ex);
        }
    }

    private MiddlewareChain buildTerminalHandlers(Update update, MaxContext context) {
        return ignored -> {
            List<ContextHandler> resolved = new ArrayList<>(anyHandlers);
            resolved.addAll(handlersByType.getOrDefault(update.updateType(), List.of()));

            String text = context.text();
            if (text != null) {
                matchHandlers(commandHandlers, text, context, resolved);
                matchHandlers(hearsHandlers, text, context, resolved);
            }
            if (update.callback() != null && update.callback().payload() != null) {
                matchHandlers(actionHandlers, update.callback().payload(), context, resolved);
            }

            for (ContextHandler handler : resolved) {
                handler.handle(context);
            }
        };
    }

    private void matchHandlers(List<PatternHandler> handlers, String value, MaxContext context, List<ContextHandler> resolved) {
        for (PatternHandler handler : handlers) {
            Matcher matcher = handler.pattern().matcher(value);
            if (matcher.find()) {
                context.matcher(matcher);
                resolved.add(handler.handler());
            }
        }
    }

    private void applyMiddlewares(MaxContext context, MiddlewareChain terminal, int index) throws Exception {
        if (index >= middlewares.size()) {
            terminal.proceed(context);
            return;
        }
        Middleware middleware = middlewares.get(index);
        middleware.handle(context, nextContext -> applyMiddlewares(nextContext, terminal, index + 1));
    }
}
