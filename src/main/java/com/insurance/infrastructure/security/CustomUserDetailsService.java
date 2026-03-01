package com.insurance.infrastructure.security;

import com.insurance.domain.entity.User;
import com.insurance.domain.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * Implementación de UserDetailsService para cargar detalles de usuario desde BD
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final IUserRepository userRepository;

    /**
     * Carga detalles del usuario por email
     * @param email Email del usuario (se usa como username)
     * @return UserDetails con authorities basadas en el rol
     * @throws UsernameNotFoundException si el usuario no existe
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Cargando usuario por email: {}", email);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado: {}", email);
                    return new UsernameNotFoundException("Usuario no encontrado: " + email);
                });

        log.debug("Usuario cargado: {}, role: {}", email, user.getRole());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getEnabled(),
                user.getAccountNonExpired(),
                user.getCredentialsNonExpired(),
                user.getAccountNonLocked(),
                getAuthorities(user.getRole())
        );
    }

    /**
     * Carga usuario por ID
     * @param userId ID del usuario
     * @return UserDetails con authorities
     * @throws UsernameNotFoundException si no existe
     */
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        log.debug("Cargando usuario por ID: {}", userId);

        User user = userRepository.findByIdAndEnabled(userId)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado: {}", userId);
                    return new UsernameNotFoundException("Usuario no encontrado: " + userId);
                });

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getEnabled(),
                user.getAccountNonExpired(),
                user.getCredentialsNonExpired(),
                user.getAccountNonLocked(),
                getAuthorities(user.getRole())
        );
    }

    /**
     * Convierte el rol de usuario a colección de GrantedAuthority
     * @param role Rol del usuario
     * @return Colección con autoridad basada en el rol
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User.UserRole role) {
        return Collections.singletonList(
                new SimpleGrantedAuthority(role.getAuthority())
        );
    }
}
