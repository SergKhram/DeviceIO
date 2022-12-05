package io.github.sergkhram.data.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@EqualsAndHashCode
public abstract class AbstractEntity {
    @Id
    protected String id;
}
