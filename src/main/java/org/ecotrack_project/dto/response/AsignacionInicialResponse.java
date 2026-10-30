package org.ecotrack_project.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class AsignacionInicialResponse {

    private int idAsignacionInicial;
    private BigDecimal montoAsignadoInicial;
}