package com.essencedetoi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank
    @Size(min = 3, max = 100)
    @Column(nullable = false)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Size(min = 6, max = 100) // La contraseña se almacenará hasheada
    @Column(nullable = false)
    private String password;

    @Column(length = 15)
    private String phoneNumber;

    private boolean enabled = true; // Para activar/desactivar usuarios

    @Column(columnDefinition = "TEXT")
    private String specialties; // Especialidades del estilista

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // Constructor (se pueden agregar más según necesidad)
    public User(String username, String fullName, String email, String password) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.enabled = true;
    }

    public void addRole(Role role) {
        this.roles.add(role);
        // role.getUsers().add(this); // Si tuvieras una relación bidireccional mapeada en Role
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
        // role.getUsers().remove(this);
    }

    // Lombok generará getters y setters
}
