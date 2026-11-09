package org.ecotrack_project.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EjecucionRecojoResponse {

    private String estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFinalizacion;

    private BigDecimal pesoTotal;
    private String observacion;

    private List<MaterialRecuperadoResponse> materiales;
}