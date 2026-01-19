package com.pos.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name"})
})
@Getter
@Setter
public class ClientPojo extends AbstractPojo {
    //name should be Client not clientpojo
    //todo- version, created_at, updated_at in abstract pojo
    //read about generation types
    //address entity to be added , city,street, pincode
    //opt locking
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    protected Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;
}