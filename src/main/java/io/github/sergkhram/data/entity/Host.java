package io.github.sergkhram.data.entity;

import lombok.Builder;
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "host")
public class Host extends AbstractEntity{
    @NotEmpty
    @Column(name = "name")
    private String name;
    @NotEmpty
    @Column(name = "address")
    private String address;
    @Column(name = "port")
    private Integer port;
    @Builder.Default
    @Column(name = "isActive")
    private Boolean isActive = false;

    @OneToMany(mappedBy = "host")
    @Nullable
    private List<Device> devices;

    public Host() {};

    public Host(String name, String address, Integer port) {
        this.name = name;
        this.address = address;
        this.port = port;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public List<Device> getDevices() {
        return devices;
    }
}
