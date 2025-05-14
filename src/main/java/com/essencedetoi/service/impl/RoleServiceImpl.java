package com.essencedetoi.service.impl;

import com.essencedetoi.model.Role;
import com.essencedetoi.repository.RoleRepository;
import com.essencedetoi.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional // Es buena práctica para métodos de servicio que interactúan con la BD
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> findAllRoles() {
        return roleRepository.findAll(); // Asume que RoleRepository tiene el método findAll()
    }

    @Override
    public Role findByName(String name) {
        // Asume que RoleRepository tiene un método como findByName(String name)
        // Si no, puedes implementarlo aquí o añadirlo al repositorio
        return roleRepository.findByName(name).orElse(null); // Devuelve null si no se encuentra
    }
    
    // Implementa otros métodos de la interfaz aquí si es necesario
}
