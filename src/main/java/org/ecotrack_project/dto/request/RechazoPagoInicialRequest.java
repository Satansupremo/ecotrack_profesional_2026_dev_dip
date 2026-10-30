
package org.ecotrack_project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RechazoPagoInicialRequest {

    @NotBlank(message = "La descripción del rechazo es obligatoria")
    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    private String descripcion;
}
