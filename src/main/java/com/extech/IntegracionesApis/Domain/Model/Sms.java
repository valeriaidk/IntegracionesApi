package com.extech.IntegracionesApis.Domain.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@Entity
@Table(name = "IT_Sms")
@NoArgsConstructor
@AllArgsConstructor
public class Sms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SmsId")
    private Long smsId;

    @Column(name = "PhoneNumber", length = 20, nullable = false)
    private String phoneNumber;

    @Column(name = "Message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "SenderId", length = 50)
    private String senderId;

    @Column(name = "CampaignName", length = 100)
    private String campaignName;

    @Column(name = "MessageId", length = 100)
    private String messageId;

    @Column(name = "StatusCode", length = 20)
    private String statusCode;

    @Column(name = "StatusMessage", length = 500)
    private String statusMessage;

    @Column(name = "ErrorCode", length = 50)
    private String errorCode;

    @Column(name = "ErrorMessage", length = 500)
    private String errorMessage;

    @Column(name = "Provider", length = 50, nullable = false)
    private String provider;

    @Column(name = "Success", nullable = false)
    private Boolean success;

    @Column(name = "FechaEnvio", nullable = false)
    private LocalDateTime fechaEnvio;

    @Column(name = "FechaRegistro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "UsuarioRegistro")
    private Integer usuarioRegistro;

    @Column(name = "UsuarioModificacion")
    private Integer usuarioModificacion;

    @Column(name = "FechaModificacion")
    private LocalDateTime fechaModificacion;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (fechaRegistro == null) {
            fechaRegistro = now;
        }
        if (fechaEnvio == null) {
            fechaEnvio = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}
