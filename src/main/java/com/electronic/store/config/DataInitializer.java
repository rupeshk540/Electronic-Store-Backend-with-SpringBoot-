package com.electronic.store.config;

import java.util.List;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.electronic.store.entities.Role;
import com.electronic.store.entities.User;
import com.electronic.store.repositories.RoleRepository;
import com.electronic.store.repositories.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {

        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleId(UUID.randomUUID().toString());
                    role.setName("ROLE_ADMIN");
                    return roleRepository.save(role);
                });

        Role normalRole = roleRepository.findByName("ROLE_NORMAL")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleId(UUID.randomUUID().toString());
                    role.setName("ROLE_NORMAL");
                    return roleRepository.save(role);
                });

        if(userRepository.findByEmail("rupesh@gmail.com").isEmpty()) {

            User admin = new User();
            admin.setUserId(UUID.randomUUID().toString());
            admin.setName("Rupesh");
            admin.setEmail("rupesh@gmail.com");
            admin.setPassword(passwordEncoder.encode("@rup321"));
            admin.setRoles(List.of(adminRole));

            userRepository.save(admin);
        }

    }
}