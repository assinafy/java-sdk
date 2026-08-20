package com.assinafy.sdk.request;

import com.assinafy.sdk.exceptions.ValidationException;
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

    @Test
    void rejectsPagingOutsideDocumentedBounds() {
        assertThatThrownBy(() -> ListParams.builder().page(0).build().toQueryParams())
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ListParams.builder().perPage(0).build().toQueryParams())
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ListParams.builder().perPage(101).build().toQueryParams())
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ListParams.builder().extra("page", 0).build().toQueryParams())
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ListParams.builder().extra("per-page", 101).build().toQueryParams())
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validatesPagingAfterCustomParametersOverrideTypedValues() {
        ListParams params = ListParams.builder()
                .page(1)
                .perPage(25)
                .extra("page", 3)
                .extra("per-page", 50)
                .build();

        assertThat(params.toQueryParams())
                .containsEntry("page", 3)
                .containsEntry("per-page", 50);

        params.getExtra().put("page", "3");
        assertThatThrownBy(params::toQueryParams)
                .isInstanceOf(ValidationException.class)
                .hasMessage("Page must be an integer");

        params.getExtra().put("page", 3);
        params.getExtra().put("per-page", null);
        assertThatThrownBy(params::toQueryParams)
                .isInstanceOf(ValidationException.class)
                .hasMessage("Per-page must be an integer");
    }

    @Test
    void rejectsBlankCustomKeysThroughBuilderAndMutableMap() {
        assertThatThrownBy(() -> ListParams.builder().extra(null, "value"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Extra query parameter key must not be blank");
        assertThatThrownBy(() -> ListParams.builder().extra("   ", "value"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Extra query parameter key must not be blank");

        ListParams params = new ListParams();
        params.getExtra().put("", "value");
        assertThatThrownBy(params::toQueryParams)
                .isInstanceOf(ValidationException.class)
                .hasMessage("Extra query parameter key must not be blank");

        params.getExtra().clear();
        params.getExtra().put("deployment-region", "south-america-east1");
        assertThat(params.toQueryParams())
                .containsOnly(entry("deployment-region", "south-america-east1"));
    }
}
