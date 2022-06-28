package io.github.sergkhram.data.enums;

public enum IOSPackageType {
    APPLICATION("--application"),
    CRASHES("--crashes"),
    MEDIA("--media"),
    ROOT("--root"),
    DISK_IMAGES("--disk-images"),
    WALLPAPER("--wallpaper");

    public String value;
    IOSPackageType(String value) {
        this.value = value;
    }
}
