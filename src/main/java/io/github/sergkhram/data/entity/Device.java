package io.github.sergkhram.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.sergkhram.data.enums.DeviceType;
import io.github.sergkhram.data.enums.OsType;
import io.github.sergkhram.grpc.converters.ProtoConverter;
import io.github.sergkhram.proto.DeviceProto;
import lombok.EqualsAndHashCode;
import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "device")
@EqualsAndHashCode(callSuper = true)
@ProtoClass(DeviceProto.class)
public class Device extends AbstractEntity {
    @Column(name = "serial")
    @ProtoField
    private String serial;
    @Column(name = "isActive")
    @ProtoField
    private Boolean isActive = false;
    @ManyToOne
    @JoinColumn(name = "host_id", referencedColumnName = "id", nullable = false)
    @NotNull
    @JsonIgnoreProperties({"devices"})
    @ProtoField(converter = ProtoConverter.DeviceHostConverter.class)
    private Host host;
    @Column(name = "osType")
    @ProtoField(converter = ProtoConverter.OsTypeConverter.class)
    private OsType osType;
    @Column(name = "state")
    @ProtoField
    private String state;
    @Column(name = "name")
    @ProtoField
    private String name;
    @Column(name = "deviceType")
    @ProtoField(converter = ProtoConverter.DeviceTypeConverter.class)
    private DeviceType deviceType;
    @Column(name = "osVersion")
    @ProtoField
    private String osVersion;

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getSerial() {
        return serial;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public Host getHost() {
        return host;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public OsType getOsType() {
        return osType;
    }

    public void setOsType(OsType osType) {
        this.osType = osType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getOsVersion() {
        return this.osVersion;
    }
}
