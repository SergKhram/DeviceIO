package io.github.sergkhram.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "device")
@EqualsAndHashCode(callSuper = true)
public class Device extends AbstractEntity {
    @Column(name = "serial")
    private String serial;
    @Column(name = "isActive")
    private Boolean isActive = false;
    @ManyToOne
    @JoinColumn(name = "host_id", referencedColumnName = "id", nullable = false)
    @NotNull
    @JsonIgnoreProperties({"devices"})
    private Host host;
    @Column(name = "deviceType")
    private DeviceType deviceType;
    @Column(name = "state")
    private String state;
    @Column(name = "name")
    private String name;

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

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
