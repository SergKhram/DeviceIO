package io.github.sergkhram.data.entity;

import lombok.Getter;

@Getter
public class DeviceDirectoryElement {
    public Boolean isDirectory;
    public String name;
    public String path;
    public String size;

    public DeviceDirectoryElement() {}
    public DeviceDirectoryElement(String name, String path) {
        this.name = name;
        this.path = path;
    }
}
