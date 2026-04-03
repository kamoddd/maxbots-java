package io.kimconsulting.maxbots.core.polling;

import io.kimconsulting.maxbots.api.http.MaxApi;
import io.kimconsulting.maxbots.api.model.Update;
import io.kimconsulting.maxbots.api.model.UpdatesPage;
import io.kimconsulting.maxbots.api.request.GetUpdatesRequest;
import io.kimconsulting.maxbots.core.bot.MaxBot;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LongPollingBotRunner implements AutoCloseable {
    private final MaxApi api;
    private final MaxBot bot;
    private final PollingOptions options;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "maxbots-long-polling");
        thread.setDaemon(true);
        return thread;
    });
    private volatile Long marker;

    public LongPollingBotRunner(MaxApi api, MaxBot bot, PollingOptions options) {
        this.api = api;
        this.bot = bot;
        this.options = options;
    }

    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        executor.submit(this::loop);
    }

    public void stop() {
        running.set(false);
        executor.shutdownNow();
    }

    public boolean isRunning() {
        return running.get();
    }

    @Override
    public void close() {
        stop();
    }

    private void loop() {
        while (running.get()) {
            try {
                UpdatesPage page = api.getUpdates(GetUpdatesRequest.builder()
                    .marker(marker)
                    .limit(options.limit())
                    .timeout(options.timeoutSeconds())
                    .updateTypes(options.updateTypes())
                    .build());
                if (page != null) {
                    handlePage(page);
                }
            } catch (Throwable error) {
                options.errorHandler().handle(error, marker);
                sleepQuietly(options.retryDelay().toMillis());
            }
        }
    }

    private void handlePage(UpdatesPage page) {
        if (page.marker() != null) {
            marker = page.marker();
        }
        List<Update> updates = page.updates();
        if (updates == null) {
            return;
        }
        for (Update update : updates) {
            bot.handle(update);
        }
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
