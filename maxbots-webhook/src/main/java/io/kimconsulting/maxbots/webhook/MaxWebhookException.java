package io.kimconsulting.maxbots.webhook;

public final class MaxWebhookException extends RuntimeException {
    public MaxWebhookException(String message) {
        super(message);
    }

    public MaxWebhookException(String message, Throwable cause) {
        super(message, cause);
    }
}
