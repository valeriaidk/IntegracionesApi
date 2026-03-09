package com.extech.IntegracionesApis.Repository.User;

import com.extech.IntegracionesApis.Domain.Model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByEmailAndActivo(String email, Boolean activo);

    boolean existsByEmail(String email);
}
