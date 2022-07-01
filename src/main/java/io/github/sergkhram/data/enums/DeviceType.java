package io.github.sergkhram.data.enums;

public enum DeviceType {
    DEVICE("device"),
    SIMULATOR("simulator");

    public String value;
    DeviceType(String value) {
        this.value = value;
    }
}
