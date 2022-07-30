package io.github.sergkhram.data.entity;

import io.github.sergkhram.proto.AppDescriptionProto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;

@SuperBuilder
@Getter
@AllArgsConstructor
@ProtoClass(AppDescriptionProto.class)
public class AppDescription {
    @ProtoField
    private String appPackage;
    @ProtoField
    private String name;
    @ProtoField
    private String path;
    @ProtoField
    private String appState;
    @ProtoField
    private Boolean isActive;
}
