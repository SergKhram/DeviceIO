package io.github.sergkhram.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "device")
@EqualsAndHashCode(callSuper = true)
public class Device extends AbstractEntity {
    @Column(name = "name")
    private String name;
    @Column(name = "isActive")
    private Boolean isActive = false;
    @ManyToOne
    @JoinColumn(name = "host_id", referencedColumnName = "id", nullable = false)
    @NotNull
    @JsonIgnoreProperties({"devices"})
    private Host host;

    @Column(name = "state")
    private String state;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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
}
