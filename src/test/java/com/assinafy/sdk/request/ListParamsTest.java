package com.assinafy.sdk.request;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Pins the exact query-parameter key spellings emitted by {@link ListParams}. These are
 * conformance-critical: {@code per-page} is hyphenated while {@code include_inactive}/
 * {@code include_standard} are underscored, matching the API, and an IDE rename must not
 * silently change them.
 */
class ListParamsTest {

    @Test
    void emitsExactKeySetForAllFields() {
        Map<String, Object> q = ListParams.builder()
                .page(2)
                .perPage(25)
                .search("contract")
                .sort("-created_at")
                .status("pending_signature")
                .method("virtual")
                .tags("t1,t2")
                .includeInactive(true)
                .includeStandard(false)
                .extra("custom", "x")
                .build()
                .toQueryParams();

        assertThat(q).containsOnlyKeys(
                "page", "per-page", "search", "sort", "status", "method",
                "tags", "include_inactive", "include_standard", "custom");
        assertThat(q.get("per-page")).isEqualTo(25);
        assertThat(q.get("page")).isEqualTo(2);
        assertThat(q.get("include_inactive")).isEqualTo(true);
        assertThat(q.get("include_standard")).isEqualTo(false);
    }

    @Test
    void omitsUnsetAndBlankValues() {
        Map<String, Object> q = ListParams.builder()
                .page(1)
                .search("   ")
                .sort("")
                .build()
                .toQueryParams();

        assertThat(q).containsOnlyKeys("page");
    }

    @Test
    void emptyParamsProduceEmptyMap() {
        assertThat(new ListParams().toQueryParams()).isEmpty();
    }
}
