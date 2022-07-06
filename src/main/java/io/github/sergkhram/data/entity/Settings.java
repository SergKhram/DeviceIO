package io.github.sergkhram.data.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Min;

@Entity
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@Table(name = "settings")
public class Settings extends AbstractEntity {
    private String androidHomePath;
    @Min(5000)
    private Integer adbTimeout;
    private String downloadPath;
}
