package io.kimconsulting.maxbots.api.request;

import io.kimconsulting.maxbots.api.model.UpdateType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record GetUpdatesRequest(
    Long marker,
    Integer limit,
    Integer timeout,
    List<UpdateType> updateTypes
) {
    public static Builder builder() {
        return new Builder();
    }

    public Map<String, Object> toQueryMap() {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("marker", marker);
        query.put("limit", limit);
        query.put("timeout", timeout);
        query.put("update_types", updateTypes);
        return query;
    }

    public static final class Builder {
        private Long marker;
        private Integer limit = 100;
        private Integer timeout = 30;
        private List<UpdateType> updateTypes;

        public Builder marker(Long marker) {
            this.marker = marker;
            return this;
        }

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder updateTypes(List<UpdateType> updateTypes) {
            this.updateTypes = updateTypes;
            return this;
        }

        public GetUpdatesRequest build() {
            return new GetUpdatesRequest(marker, limit, timeout, updateTypes);
        }
    }
}
