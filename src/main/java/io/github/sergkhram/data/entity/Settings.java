package io.github.sergkhram.data.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Min;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@Document(collection = "settings")
public class Settings extends AbstractEntity {
    private String androidHomePath;
    @Min(5000)
    private Integer adbTimeout;
    private String downloadPath;
}
