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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id; // Standard ID inherited by all

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt; // Useful for sorting your Bootstrap tables

    @UpdateTimestamp
    @Column(nullable = false)
    private ZonedDateTime updatedAt; // Track when an 'Inline Edit' last occurred

    @Version
    private Integer version; // Prevents "lost updates" if two users edit at once
}