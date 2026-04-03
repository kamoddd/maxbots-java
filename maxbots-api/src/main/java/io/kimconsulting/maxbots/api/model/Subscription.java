package io.kimconsulting.maxbots.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Subscription(
    String url,
    List<UpdateType> updateTypes,
    String secret
) {
}
