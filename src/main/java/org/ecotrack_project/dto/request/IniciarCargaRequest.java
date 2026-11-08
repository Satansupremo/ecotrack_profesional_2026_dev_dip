package org.ecotrack_project.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IniciarCargaRequest {

    @NotNull(message = "Debe seleccionar una salida.")
    private Integer idCola;

    @NotNull(message = "El peso de la carga es obligatorio.")
    @DecimalMin(value = "0.01", message = "El peso debe ser mayor que cero.")
    private BigDecimal pesoLibras;
}