package io.github.sergkhram.data.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@AllArgsConstructor
public class AppDescription {
    private String appPackage;
    private String name;
    private String path;
    private String appState;
    private Boolean isActive;
}
