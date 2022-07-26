package io.github.sergkhram.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.NumberFormat;

import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "host")
public class Host extends AbstractEntity {
    @NotEmpty
    @Column(name = "name")
    private String name;
    @NotEmpty
    @Column(name = "address")
    private String address;
    @Column(name = "port")
    @NumberFormat
    @Max(65535)
    @Min(0)
    private Integer port;
    @Builder.Default
    @Column(name = "isActive")
    private Boolean isActive = false;

    @OneToMany(mappedBy = "host")
    @Nullable
    @JsonIgnoreProperties({"host"})
    private List<Device> devices;

    public Host() {}

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

    private void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj==null) return false;
        Host current = this;
        Host another = (Host) obj;
        String currentPort = current.getPort()!=null ? current.getPort().toString() : "";
        String anotherPort = another.getPort()!=null ? another.getPort().toString() : "";
        return current.getName().equals(another.getName())
            && current.getAddress().equals(another.getAddress())
            && currentPort.equals(anotherPort)
            && current.getId().equals(another.getId());
    }
}
