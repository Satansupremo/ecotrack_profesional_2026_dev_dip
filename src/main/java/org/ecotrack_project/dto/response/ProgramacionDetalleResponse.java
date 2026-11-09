package org.ecotrack_project.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public final class ProgramacionDetalleResponse {

    private Integer idProgramacion;
    private String estado;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaEjecucion;
    private String observacion;
    private LocalDateTime fechaFinalizacion;
    private List<UnidadProgramacionResponse> unidades;
}