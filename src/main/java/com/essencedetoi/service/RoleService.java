package com.essencedetoi.service;

import com.essencedetoi.model.Role;
import java.util.List;

public interface RoleService {
    List<Role> findAllRoles();
    Role findByName(String name); // Podría ser útil
    // Otros métodos que puedas necesitar, como findById, save, etc.
}
