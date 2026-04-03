package io.kimconsulting.maxbots.api.request;

import io.kimconsulting.maxbots.api.model.UpdateType;
import java.util.List;
import java.util.Objects;

public record CreateSubscriptionRequest(
    String url,
    String secret,
    List<UpdateType> updateTypes
) {
    public CreateSubscriptionRequest {
        Objects.requireNonNull(url, "url");
    }
}
