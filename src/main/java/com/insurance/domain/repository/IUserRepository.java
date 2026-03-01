package com.insurance.domain.repository;

import com.insurance.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para gestionar entidades User
 */
@Repository
public interface IUserRepository extends JpaRepository<User, Long> {

    /**
     * Busca usuario por email
     * @param email Email del usuario
     * @return Optional con el usuario si existe
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.enabled = true")
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);

    /**
     * Verifica si existe un usuario con el email especificado
     * @param email Email a verificar
     * @return true si existe, false en otro caso
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    /**
     * Busca usuario por ID y verifica que esté habilitado
     * @param id ID del usuario
     * @return Optional con el usuario si existe y está habilitado
     */
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.enabled = true")
    Optional<User> findByIdAndEnabled(@Param("id") Long id);

    /**
     * Cuenta usuarios con rol específico
     * @param role Rol a contar
     * @return Número de usuarios con ese rol
     */
    long countByRole(User.UserRole role);
}
