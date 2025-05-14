package com.essencedetoi.service.impl;

import com.essencedetoi.dto.UserRegistrationDto;
import com.essencedetoi.model.Role;
import com.essencedetoi.model.User;
import com.essencedetoi.repository.RoleRepository;
import com.essencedetoi.repository.UserRepository;
import com.essencedetoi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User save(UserRegistrationDto registrationDto) {
        return registerNewUser(registrationDto, "ROLE_CLIENT"); // Por defecto, los registros son clientes
    }

    @Override
    @Transactional
    public User registerNewUser(UserRegistrationDto registrationDto, String roleName) {
        if (existsByUsername(registrationDto.getUsername())) {
            throw new IllegalArgumentException("Ya existe un usuario con el nombre de usuario: " + registrationDto.getUsername());
        }
        if (existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + registrationDto.getEmail());
        }

        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setFullName(registrationDto.getFullName());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setPhoneNumber(registrationDto.getPhoneNumber());
        user.setEnabled(true);

        Role userRole = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role(roleName);
                    return roleRepository.save(newRole);
                });
        user.setRoles(new HashSet<>(Collections.singletonList(userRole)));

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User updateUserProfile(Long userId, String fullName, String email, String phoneNumber, String specialties) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Validar que el email no esté en uso por otro usuario
        if (!user.getEmail().equals(email) && existsByEmail(email)) {
            throw new IllegalArgumentException("El correo electrónico ya está en uso");
        }

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);

        // Solo actualizar especialidades si el usuario es un estilista
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_STYLIST"))) {
            user.setSpecialties(specialties);
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAllUsers() {
        return userRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllClients() {
        Role clientRole = roleRepository.findByName("ROLE_CLIENT").orElse(null);
        if (clientRole == null) return Collections.emptyList();
        // Esto es una simplificación. Una mejor manera sería tener una query en el UserRepository
        return userRepository.findAll().stream()
               .filter(user -> user.getRoles().contains(clientRole))
               .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllStylists() {
        Role stylistRole = roleRepository.findByName("ROLE_STYLIST").orElse(null);
        if (stylistRole == null) return Collections.emptyList();
        return userRepository.findAll().stream()
               .filter(user -> user.getRoles().contains(stylistRole))
               .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public User updateUser(Long id, User userDetails) { // Considerar DTO para actualización
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con id: " + id));
        
        existingUser.setFullName(userDetails.getFullName());
        existingUser.setEmail(userDetails.getEmail()); // Añadir lógica para verificar unicidad si cambia
        existingUser.setPhoneNumber(userDetails.getPhoneNumber());
        existingUser.setEnabled(userDetails.isEnabled());
        // Considerar cómo manejar el cambio de contraseña y roles aquí
        // Por ejemplo, si userDetails.getPassword() no es nulo y no está vacío, actualizarlo.
        // if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
        //    existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        // }
        // if (userDetails.getRoles() != null && !userDetails.getRoles().isEmpty()) {
        //    existingUser.setRoles(userDetails.getRoles());
        // }
        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UsernameNotFoundException("Usuario no encontrado con id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Implementación de UserDetailsService (reutiliza la de UserDetailsServiceImpl o se integra aquí)
    // Para evitar duplicación, SecurityConfig ya usa UserDetailsServiceImpl.
    // Si esta clase UserService va a ser el UserDetailsService primario, entonces este método es necesario.
    // Por ahora, lo dejamos comentado ya que UserDetailsServiceImpl ya cumple esa función.
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
    System.out.println("Attempting to load user by: " + usernameOrEmail);
    User user = userRepository.findByUsername(usernameOrEmail)
            .orElseGet(() -> {
                System.out.println("User not found by username, trying by email: " + usernameOrEmail);
                return userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> {
                            System.out.println("User not found by username or email: " + usernameOrEmail);
                            return new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail);
                        });
            });
    System.out.println("User found: " + user.getUsername() + ", Enabled: " + user.isEnabled());

        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true, 
                true, 
                true, 
                authorities
        );
    }

    @Override
    @Transactional
    public void updateUserRolesAndStatus(Long userId, List<Long> roleIds, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con id: " + userId));

        Set<Role> newRoles = new HashSet<>();
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con id: " + roleId));
                newRoles.add(role);
            }
        }
        // Asegurarse de que al menos un rol esté presente si la lógica de negocio lo requiere.
        // Por ejemplo, si se deseleccionan todos los roles, ¿qué debería pasar?
        // Podrías añadir una validación aquí, por ejemplo, si newRoles está vacío y eso no está permitido.
        // if (newRoles.isEmpty()) {
        //    throw new IllegalArgumentException("Un usuario debe tener al menos un rol.");
        // }

        user.setRoles(newRoles);
        user.setEnabled(enabled);
        userRepository.save(user);
    }
}
