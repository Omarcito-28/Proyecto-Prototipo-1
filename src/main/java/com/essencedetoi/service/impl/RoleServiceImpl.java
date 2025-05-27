package com.essencedetoi.service.impl;

import com.essencedetoi.model.Role;
import com.essencedetoi.repository.RoleRepository;
import com.essencedetoi.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional 
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> findAllRoles() {
        return roleRepository.findAll(); 
    }

    @Override
    public Role findByName(String name) {
    
        return roleRepository.findByName(name).orElse(null); // Devuelve null si no se encuentra
    }
    
}
