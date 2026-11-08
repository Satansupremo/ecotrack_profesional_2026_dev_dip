package org.ecotrack_project.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargaProcesoResponse {

    private Integer idCarga;
    private Integer idCola;
    private Integer idProgramacion;
    private BigDecimal pesoLibras;
    private LocalDateTime fechaRegistro;
}