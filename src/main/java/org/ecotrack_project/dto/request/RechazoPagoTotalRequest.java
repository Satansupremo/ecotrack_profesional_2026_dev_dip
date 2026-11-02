package org.ecotrack_project.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechazoPagoTotalRequest {

    @NotBlank(message = "La descripción del rechazo es obligatoria")
    private String descripcion;
}