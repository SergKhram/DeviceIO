package io.github.sergkhram.utils.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.SneakyThrows;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class JsonTestUtil {
    public static ObjectMapper objectMapper;

    static  {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        objectMapper.registerModule(module);
    }

    @SneakyThrows
    public static void compareJson(JsonNode node1, JsonNode node2) {
        compareJson(node1, node2, JSONCompareMode.LENIENT);
    }

    @SneakyThrows
    public static void compareJson(JsonNode node1, JsonNode node2, JSONCompareMode compareMode) {
        JSONAssert.assertEquals(node1.toPrettyString(), node2.toPrettyString(), compareMode);
    }

    @SneakyThrows
    public static void compareJsonWRegex(JsonNode node1, JsonNode node2) {
        compareJsonWRegex(node1, node2, JSONCompareMode.LENIENT);
    }

    @SneakyThrows
    public static void compareJsonWRegex(JsonNode node1, JsonNode node2, JSONCompareMode mode) {
        JSONAssert.assertEquals(
            node1.toString(),
            node2.toString(),
            new RegexpJsonComparator(
                mode
            )
        );
    }

    @SneakyThrows
    public static <T> JsonNode convertModelToJsonNode(T model) {
        ObjectMapper mapper = JsonTestUtil.objectMapper;
        return mapper.readValue(mapper.writeValueAsBytes(model), JsonNode.class);
    }

    @SneakyThrows
    public static <T> String convertModelToString(T model) {
        ObjectMapper mapper = JsonTestUtil.objectMapper;
        return mapper.writeValueAsString(model);
    }

    @SneakyThrows
    public static JsonNode convertStringToJsonNode(String jsonString) {
        ObjectMapper mapper = JsonTestUtil.objectMapper;
        return mapper.readTree(jsonString);
    }
}
