package com.pos.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "pos_client_name_uk", columnNames = {"name"})
})
@Getter
@Setter
public class Client extends AbstractPojo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;
}