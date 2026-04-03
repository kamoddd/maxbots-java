package io.kimconsulting.maxbots.api.http;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

public interface RawMaxApiClient {
    JsonNode get(String path, Map<String, ?> query);

    JsonNode post(String path, Map<String, ?> query, Object body);

    JsonNode put(String path, Map<String, ?> query, Object body);

    JsonNode patch(String path, Map<String, ?> query, Object body);

    JsonNode delete(String path, Map<String, ?> query);
}
