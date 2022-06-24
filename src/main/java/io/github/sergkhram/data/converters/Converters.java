package io.github.sergkhram.data.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sergkhram.data.json.JsonUtil;
import lombok.SneakyThrows;

public class Converters {
    @SneakyThrows
    public static <T> JsonNode convertModelToJsonNode(T model) {
        ObjectMapper mapper = JsonUtil.objectMapper;
        return mapper.readValue(mapper.writeValueAsBytes(model), JsonNode.class);
    }

    @SneakyThrows
    public static <T> String convertModelToString(T model) {
        ObjectMapper mapper = JsonUtil.objectMapper;
        return mapper.writeValueAsString(model);
    }

    @SneakyThrows
    public static JsonNode convertStringToJsonNode(String jsonString) {
        ObjectMapper mapper = JsonUtil.objectMapper;
        return mapper.readTree(jsonString);
    }
}
