package com.extech.IntegracionesApis.Repository.Auth;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class AuthSpRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> validarAcceso(String email) {
        String sql = "EXEC dbo.uspUsuarioValidarAcceso @Email = ?";
        return jdbcTemplate.queryForList(sql, email);
    }

    public Integer insertarTokenUsuario(
            Integer usuarioId,
            String tokenHash,
            Timestamp fechaInicioVigencia,
            Timestamp fechaFinVigencia,
            Integer usuarioRegistro
    ) {
        String sql = "EXEC dbo.usp_InsertarTokenUsuario " +
                "@UsuarioId = ?, " +
                "@TokenHash = ?, " +
                "@FechaInicioVigencia = ?, " +
                "@FechaFinVigencia = ?, " +
                "@UsuarioRegistro = ?";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                sql,
                usuarioId,
                tokenHash,
                fechaInicioVigencia,
                fechaFinVigencia,
                usuarioRegistro
        );

        if (rows.isEmpty()) {
            return null;
        }

        Map<String, Object> first = rows.get(0);
        Object tokenId = first.values().stream().findFirst().orElse(null);

        if (tokenId instanceof Number number) {
            return number.intValue();
        }

        return null;
    }

    public List<Map<String, Object>> obtenerVigentesPorTokenUsuario(Integer usuarioId) {
        String sql = "EXEC dbo.uspObtenerVigentesPorTokenUsuario @UsuarioId = ?";
        return jdbcTemplate.queryForList(sql, usuarioId);
    }
}
