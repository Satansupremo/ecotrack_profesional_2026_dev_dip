package org.ecotrack_project.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargaRequest {

    @NotNull(message = "Debe seleccionar una salida pendiente.")
    private Integer idCola;

    @NotNull(message = "El peso de la carga es obligatorio.")
    @DecimalMin(value = "0.01", message = "El peso debe ser mayor que cero.")
    private BigDecimal pesoLibras;

    @NotEmpty(message = "Debe registrar al menos un artículo recuperado.")
    @Valid
    private List<ArticuloRecuperadoRequest> articulos;
}