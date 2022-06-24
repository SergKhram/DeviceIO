package io.github.sergkhram.data.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JsonUtil {
    public static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        objectMapper.registerModule(module);
    }
}