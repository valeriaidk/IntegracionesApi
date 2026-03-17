package com.extech.IntegracionesApis.Util.Security;

/**
 * Contexto de seguridad para almacenar información del usuario autenticado
 * 
 * Esta clase mantiene el UsuarioId del token validado durante todo el ciclo
 * de vida de la request, permitiendo que los servicios accedan a esta información
 * sin necesidad de volver a validar el token.
 */
public class UserContext {

    private static final ThreadLocal<Integer> usuarioId = new ThreadLocal<>();

    /**
     * Establece el ID del usuario autenticado en el contexto actual
     * 
     * @param id ID del usuario autenticado
     */
    public static void setUsuarioId(Integer id) {
        usuarioId.set(id);
    }

    /**
     * Obtiene el ID del usuario autenticado del contexto actual
     * 
     * @return ID del usuario autenticado o null si no está establecido
     */
    public static Integer getUsuarioId() {
        return usuarioId.get();
    }

    /**
     * Limpia el contexto del usuario actual
     * 
     * Este método debe llamarse al final de cada request para evitar
     * memory leaks en entornos de thread pools
     */
    public static void clear() {
        usuarioId.remove();
    }

    /**
     * Verifica si hay un usuario autenticado en el contexto actual
     * 
     * @return true si hay un usuario autenticado, false en caso contrario
     */
    public static boolean isAuthenticated() {
        return usuarioId.get() != null;
    }
}
