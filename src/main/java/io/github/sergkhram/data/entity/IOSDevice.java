package io.github.sergkhram.data.entity;

import io.github.sergkhram.data.enums.DeviceType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IOSDevice {
    private String name;
    private String serial;
    private String state;
    private DeviceType type;
    private String iosVersion;
    private String architecture;
}
