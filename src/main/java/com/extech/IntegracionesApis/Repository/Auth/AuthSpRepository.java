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

    /**
     * 📊 Obtiene datos completos del usuario incluyendo fechaRegistro
     */
    public List<Map<String, Object>> obtenerDatosCompletosUsuario(Integer usuarioId) {
        String sql = "SELECT " +
                "u.UsuarioId, " +
                "u.Nombre, " +
                "u.Apellido, " +
                "u.Email, " +
                "FORMAT(u.FechaRegistro, 'yyyy-MM-dd HH:mm:ss') as FechaRegistro, " +
                "u.Activo, " +
                "u.Eliminado, " +
                "ISNULL(p.PlanId, 1) as PlanId, " +
                "ISNULL(p.Nombre, 'FREE') as PlanNombre " +
                "FROM dbo.IT_Usuario u " +
                "LEFT JOIN dbo.IT_Plan p ON u.PlanId = p.PlanId " +
                "WHERE u.UsuarioId = ? " +
                "AND ISNULL(u.Eliminado, 0) = 0";
        return jdbcTemplate.queryForList(sql, usuarioId);
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

    /**
     * 📊 Obtiene la configuración completa del plan con sus límites y funciones
     */
    public List<Map<String, Object>> obtenerConfiguracionPlan(Integer planId) {
        String sql = "EXEC dbo.uspPlanObtenerConfiguracionCompleta @PlanId = ?";
        return jdbcTemplate.queryForList(sql, planId);
    }

    public List<Map<String, Object>> guardarOActualizarUsuario(
            Integer usuarioId,
            String nombre,
            String apellido,
            String email,
            String passwordHash,
            Integer planId,
            String telefono,
            String razonSocial,
            String ruc,
            Boolean activo,
            Boolean eliminado,
            Integer usuarioAccion
    ) {
        String sql = "EXEC dbo.uspIT_UsuarioGuardarActulizar " +
                "@UsuarioId = ?, " +
                "@Nombre = ?, " +
                "@Apellido = ?, " +
                "@Email = ?, " +
                "@PasswordHash = ?, " +
                "@PlanId = ?, " +
                "@Telefono = ?, " +
                "@RazonSocial = ?, " +
                "@RUC = ?, " +
                "@Activo = ?, " +
                "@Eliminado = ?, " +
                "@UsuarioAccion = ?";

        return jdbcTemplate.queryForList(
                sql,
                usuarioId,
                nombre,
                apellido,
                email,
                passwordHash,
                planId,
                telefono,
                razonSocial,
                ruc,
                activo,
                eliminado,
                usuarioAccion
        );
    }
}
