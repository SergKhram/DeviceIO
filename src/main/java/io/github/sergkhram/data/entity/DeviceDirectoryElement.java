package io.github.sergkhram.data.entity;

import io.github.sergkhram.proto.DeviceDirectoryElementProto;
import lombok.Getter;
import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;

@Getter
@ProtoClass(DeviceDirectoryElementProto.class)
public class DeviceDirectoryElement {
    @ProtoField
    public Boolean isDirectory;
    @ProtoField
    public String name;
    @ProtoField
    public String path;
    @ProtoField
    public String size;

    public DeviceDirectoryElement() {}
    public DeviceDirectoryElement(String name, String path) {
        this.name = name;
        this.path = path;
    }
}
