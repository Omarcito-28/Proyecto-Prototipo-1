package com.essencedetoi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String name; // e.g., ROLE_ADMIN, ROLE_CLIENT, ROLE_STYLIST

    public Role(String name) {
        this.name = name;
    }

    // No es necesario getters/setters/constructores explícitos con Lombok
    // Pero un constructor con el nombre puede ser útil.
}
