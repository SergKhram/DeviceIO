package io.github.sergkhram.utils;

import org.testcontainers.utility.DockerImageName;

public class TestConst {
    public static final DockerImageName mongoContainer = DockerImageName.parse("mongo:5.0.14");
}
