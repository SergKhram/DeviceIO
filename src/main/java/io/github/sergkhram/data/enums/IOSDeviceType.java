package io.github.sergkhram.data.enums;

public enum IOSDeviceType {
    DEVICE("device"),
    SIMULATOR("simulator");

    public String value;
    IOSDeviceType(String value) {
        this.value = value;
    }
}
