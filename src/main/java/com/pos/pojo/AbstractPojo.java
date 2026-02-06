package com.pos.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class AbstractPojo {
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    //todo check if it can be null
    @UpdateTimestamp
    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Integer version;
}