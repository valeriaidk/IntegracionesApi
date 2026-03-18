-- 🔍 VERIFICAR TOKENS ACTIVOS PARA PROBAR SMS
SELECT 
    tu.Id,
    tu.UsuarioId,
    tu.ApiKey,
    tu.FechaInicioVigencia,
    tu.FechaFinVigencia,
    tu.Activo,
    tu.Eliminado,
    u.Email,
    u.Nombre
FROM dbo.IT_TokenUsuario tu
LEFT JOIN dbo.IT_Usuario u ON tu.UsuarioId = u.UsuarioId
WHERE tu.Activo = 1 
  AND tu.Eliminado = 0
  AND tu.FechaFinVigencia > GETDATE()
ORDER BY tu.FechaRegistro DESC;

-- Si no tienes tokens, crea uno de prueba:
IF NOT EXISTS (
    SELECT 1 FROM dbo.IT_TokenUsuario 
    WHERE Activo = 1 AND Eliminado = 0 AND FechaFinVigencia > GETDATE()
)
BEGIN
    -- Crear token de prueba para usuario ID 2 (ajusta según necesites)
    INSERT INTO dbo.IT_TokenUsuario
    (UsuarioId, ApiKey, FechaInicioVigencia, FechaFinVigencia, UsuarioRegistro, FechaRegistro, Activo, Eliminado)
    VALUES
    (2, '_Gdepl-KQ30MVA54X8zFkooMZvERsw', GETDATE(), DATEADD(DAY, 365, GETDATE()), 1, GETDATE(), 1, 0);
    
    PRINT '✅ TOKEN DE PRUEBA CREADO: _Gdepl-KQ30MVA54X8zFkooMZvERsw';
END
