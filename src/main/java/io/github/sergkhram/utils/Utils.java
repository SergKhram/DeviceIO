package io.github.sergkhram.utils;

import io.github.sergkhram.managers.Manager;

import java.util.List;

public class Utils {
    public static <T extends Manager> T getManagerByType(List<Manager> managers, Class<T> type) {
        return (T) managers
            .stream()
            .filter(
                it -> type.isInstance(it)
            )
            .findFirst()
            .get();
    }
}
