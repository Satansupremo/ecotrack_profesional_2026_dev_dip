package org.ecotrack_project.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AsignacionInicialRequest {

    @NotNull(message = "El monto inicial es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto inicial debe ser mayor a cero")
    private BigDecimal montoAsignadoInicial;
}