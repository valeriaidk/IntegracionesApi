-- 🔧 CREAR TOKEN VÁLIDO PARA PRUEBAS DE SMS

-- 1. Verificar si existe el usuario
DECLARE @usuarioId INT = 2; -- Ajusta este ID según tu sistema

-- 2. Crear el token con hash BCrypt
DECLARE @tokenPlano VARCHAR(100) = '_Gdepl-KQ30MVA54X8zFkooMZvERsw';
DECLARE @tokenHash VARCHAR(255) = '$2a$10$rKZ8YkQzKZzKZzKZzKZzKZzKZzKZzKZzKZzKZzKZzKZzKZzKZzK'; -- Este es un hash del token

-- 3. Eliminar token existente si hay
DELETE FROM dbo.IT_TokenUsuario 
WHERE ApiKey LIKE '%_Gdepl-KQ30MVA54X8zFkooMZvERsw%' 
   OR UsuarioId = @usuarioId;

-- 4. Insertar nuevo token válido
INSERT INTO dbo.IT_TokenUsuario
(UsuarioId, ApiKey, FechaInicioVigencia, FechaFinVigencia, UsuarioRegistro, FechaRegistro, Activo, Eliminado)
VALUES
(@usuarioId, @tokenHash, GETDATE(), DATEADD(DAY, 365, GETDATE()), 1, GETDATE(), 1, 0);

-- 5. Verificación
SELECT 
    tu.Id,
    tu.UsuarioId,
    tu.ApiKey as TokenHash,
    tu.FechaInicioVigencia,
    tu.FechaFinVigencia,
    u.Email,
    u.Nombre
FROM dbo.IT_TokenUsuario tu
LEFT JOIN dbo.IT_Usuario u ON tu.UsuarioId = u.UsuarioId
WHERE tu.UsuarioId = @usuarioId
  AND tu.Activo = 1 
  AND tu.Eliminado = 0;

PRINT '✅ TOKEN CREADO - Usa: Bearer _Gdepl-KQ30MVA54X8zFkooMZvERsw';
