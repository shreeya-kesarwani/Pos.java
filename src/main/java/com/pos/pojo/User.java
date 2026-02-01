package com.pos.pojo;

import com.pos.model.constants.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name= "User_email_uk", columnNames = "email")
})
@Getter
@Setter
public class User extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(nullable = false)
    @Email
    private String email;

    @Column(nullable = false)
    private String passwordHash;
    //todo rename to password - which hashing algo used, which library

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    //todo- add enums in a folder called constants in model
}
