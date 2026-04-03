package io.kimconsulting.maxbots.core.polling;

import io.kimconsulting.maxbots.api.model.UpdateType;
import java.time.Duration;
import java.util.List;

public record PollingOptions(
    int limit,
    int timeoutSeconds,
    Duration retryDelay,
    List<UpdateType> updateTypes,
    PollingErrorHandler errorHandler
) {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int limit = 100;
        private int timeoutSeconds = 30;
        private Duration retryDelay = Duration.ofSeconds(2);
        private List<UpdateType> updateTypes;
        private PollingErrorHandler errorHandler = (error, marker) -> {
        };

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder timeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        public Builder retryDelay(Duration retryDelay) {
            this.retryDelay = retryDelay;
            return this;
        }

        public Builder updateTypes(List<UpdateType> updateTypes) {
            this.updateTypes = updateTypes;
            return this;
        }

        public Builder errorHandler(PollingErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        public PollingOptions build() {
            return new PollingOptions(limit, timeoutSeconds, retryDelay, updateTypes, errorHandler);
        }
    }
}
