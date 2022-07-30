package io.github.sergkhram.data.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.badata.protobuf.converter.annotation.ProtoField;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@EqualsAndHashCode
public abstract class AbstractEntity {
    @Id
    @GeneratedValue
    @Type(type = "uuid-char")
    @Column(name = "id")
    @ProtoField
    protected UUID id;
}
