package com.extech.IntegracionesApis.Domain.Dto.Auth;

public class LoginRequest {
    private String usuario;
    private String contrasena;

    // Constructores
    public LoginRequest() {}

    public LoginRequest(String usuario, String contrasena) {
        this.usuario = usuario;
        this.contrasena = contrasena;
    }

    // Getters y Setters
    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
