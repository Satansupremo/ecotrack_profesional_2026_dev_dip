package org.ecotrack_project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedioPagoRequest {

    @NotBlank(message = "El nombre del medio de pago es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String descripcion;

    @Size(max = 50, message = "El número de cuenta no puede superar los 50 caracteres")
    private String numeroCuenta;
}
