package io.github.sergkhram.data.entity;

import io.github.sergkhram.data.enums.IOSDeviceType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IOSDevice {
    private String name;
    private String serial;
    private String state;
    private IOSDeviceType type;
    private String iosVersion;
    private String architecture;
}
