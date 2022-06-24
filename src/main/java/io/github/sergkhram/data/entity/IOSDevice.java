package io.github.sergkhram.data.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IOSDevice {
    private String name;
    private String serial;
    private String state;
    private String type;
    private String iosVersion;
    private String architecture;
}
