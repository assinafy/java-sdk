package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.helper.MockApiHttpClient;
import com.assinafy.sdk.models.FieldDefinition;
import com.assinafy.sdk.models.FieldType;
import com.assinafy.sdk.request.CreateFieldRequest;
import com.assinafy.sdk.request.UpdateFieldRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class FieldResourceTest {

    private static final String FIELD_RESPONSE = "{\"status\":200,\"data\":{\"id\":\"f1\",\"name\":\"CPF\",\"type\":\"cpf\"}}";

    private MockApiHttpClient mock;
    private FieldResource resource;

    @BeforeEach
    void setUp() {
        mock = new MockApiHttpClient();
        resource = new FieldResource(mock, "acc");
    }

    @Test
    void createPostsToAccountFieldsEndpoint() {
        mock.enqueue(200, FIELD_RESPONSE);
        FieldDefinition field = resource.create(CreateFieldRequest.builder()
                .type("text")
                .name("Field Name")
                .build());

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("POST");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/fields");
        assertThat(field.getId()).isEqualTo("f1");
    }

    @Test
    void getRequiresFieldId() {
        assertThatThrownBy(() -> resource.get(""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void updatePutsToAccountFieldEndpoint() {
        mock.enqueue(200, FIELD_RESPONSE);
        resource.update("f1", UpdateFieldRequest.builder().name("New Name").build());

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("PUT");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/fields/f1");
        assertThat(mock.lastCaptured().getJsonBody()).contains("New Name");
    }

    @Test
    void deleteCallsDelete() {
        mock.enqueue(200, "{}");
        resource.delete("f1");

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("DELETE");
        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/fields/f1");
    }

    @Test
    void validateWithSignerAccessCodeAppendsQueryParam() {
        mock.enqueue(200, "{\"status\":200,\"data\":{\"success\":true}}");
        resource.validate("f1", "value", "code 1");

        assertThat(mock.lastCaptured().getMethod()).isEqualTo("POST");
        assertThat(mock.lastCaptured().getPath())
                .isEqualTo("/accounts/acc/fields/f1/validate?signer-access-code=code+1");
    }

    @Test
    void validateWithoutSignerAccessCodeOmitsQueryParam() {
        mock.enqueue(200, "{\"status\":200,\"data\":{\"success\":true}}");
        resource.validate("f1", "value", null);

        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/fields/f1/validate");
    }

    @Test
    void validateMultipleSerialisesEntries() {
        mock.enqueue(200, "{\"status\":200,\"data\":[]}");
        resource.validateMultiple(List.of(Map.of("field_id", "f1", "value", "v")), null);

        assertThat(mock.lastCaptured().getPath()).isEqualTo("/accounts/acc/fields/validate-multiple");
        assertThat(mock.lastCaptured().getJsonBody()).contains("field_id").contains("f1");
    }

    @Test
    void listTypesHitsFieldTypesEndpoint() {
        mock.enqueue(200, "{\"status\":200,\"data\":[{\"type\":\"cpf\",\"name\":\"CPF\"}]}");
        List<FieldType> types = resource.listTypes();

        assertThat(mock.lastCaptured().getPath()).isEqualTo("/field-types");
        assertThat(types).hasSize(1);
        assertThat(types.get(0).getType()).isEqualTo("cpf");
    }
}
