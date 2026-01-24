package com.pos.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
    private ZonedDateTime createdAt; // Useful for sorting your Bootstrap tables

    @UpdateTimestamp
    @Column(nullable = false)
    private ZonedDateTime updatedAt; // Track when an 'Inline Edit' last occurred

    @Version
    @Column(nullable = false)
    private Integer version; // Prevents "lost updates" if two users edit at once
}