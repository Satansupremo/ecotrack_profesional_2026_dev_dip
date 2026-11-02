package org.ecotrack_project.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class AsignacionTotalResponse {

    private int idAsignacionTotal;
    private BigDecimal montoAsignadoTotal;
    private BigDecimal montoPorCobrar;
    private String razon;
}