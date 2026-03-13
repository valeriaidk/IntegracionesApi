package com.extech.IntegracionesApis.Domain.Dto.Auth;

public class LoginResponse {
    private String token;
    private String tipo = "Bearer";
    private UsuarioData usuario;

    public static class UsuarioData {
        private String name;
        private String username;
        private String fullName;
        private String email;
        private String joined;
        private boolean verified;
        private String plan;

        // Constructores
        public UsuarioData() {}

        public UsuarioData(String name, String username, String fullName, String email, String joined, boolean verified, String plan) {
            this.name = name;
            this.username = username;
            this.fullName = fullName;
            this.email = email;
            this.joined = joined;
            this.verified = verified;
            this.plan = plan;
        }

        // Getters y Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getJoined() { return joined; }
        public void setJoined(String joined) { this.joined = joined; }

        public boolean isVerified() { return verified; }
        public void setVerified(boolean verified) { this.verified = verified; }

        public String getPlan() { return plan; }
        public void setPlan(String plan) { this.plan = plan; }
    }

    // Constructores
    public LoginResponse() {}

    public LoginResponse(String token, UsuarioData usuario) {
        this.token = token;
        this.usuario = usuario;
    }

    // Getters y Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public UsuarioData getUsuario() { return usuario; }
    public void setUsuario(UsuarioData usuario) { this.usuario = usuario; }
}
