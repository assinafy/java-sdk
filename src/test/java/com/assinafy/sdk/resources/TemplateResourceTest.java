package com.assinafy.sdk.resources;

import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Template;
import com.assinafy.sdk.models.TemplateListItem;
import com.assinafy.sdk.request.ListParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/** Unit coverage for {@link TemplateResource} (previously only touched by the live smoke IT). */
class TemplateResourceTest {

    private MockApiHttpClient http;
    private TemplateResource templates;

    @BeforeEach
    void setUp() {
        http = new MockApiHttpClient();
        templates = new TemplateResource(http, "acc");
    }

    @Test
    void listHitsTemplatesPathAndParsesItems() {
        http.enqueue(200,
                "{\"status\":200,\"data\":[{\"id\":\"t1\",\"name\":\"NDA.pdf\",\"status\":\"Ready\"}]}",
                Map.of("x-pagination-total-count", "1", "x-pagination-per-page", "20"));

        PaginatedResult<TemplateListItem> result =
                templates.list(ListParams.builder().perPage(20).build());

        assertThat(http.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/accounts/acc/templates");
        assertThat(http.lastCaptured().getQueryParams()).containsEntry("per-page", 20);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getName()).isEqualTo("NDA.pdf");
        assertThat(result.getMeta().getTotal()).isEqualTo(1);
    }

    @Test
    void getHitsSingleTemplatePath() {
        http.enqueue(200, "{\"status\":200,\"data\":{\"id\":\"t1\",\"name\":\"NDA.pdf\"}}");

        Template t = templates.get("t1");

        assertThat(http.lastCaptured().getMethod()).isEqualTo("GET");
        assertThat(http.lastCaptured().getPath()).isEqualTo("/accounts/acc/templates/t1");
        assertThat(t.getId()).isEqualTo("t1");
    }
}
