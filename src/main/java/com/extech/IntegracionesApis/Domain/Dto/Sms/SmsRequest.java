package com.extech.IntegracionesApis.Domain.Dto.Sms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest {
    
    @NotBlank(message = "El número de teléfono es obligatorio")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "El formato del número de teléfono no es válido")
    private String phoneNumber;
    
    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 160, message = "El mensaje no puede exceder los 160 caracteres")
    private String message;
    
    private String senderId;
    
    private String campaignName;
}
