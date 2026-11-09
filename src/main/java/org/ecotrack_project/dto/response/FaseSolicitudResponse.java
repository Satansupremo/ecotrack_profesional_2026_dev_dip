package org.ecotrack_project.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FaseSolicitudResponse {

    private String nombreFase;
    private String descripcion;
    private String estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFinalizacion;
}