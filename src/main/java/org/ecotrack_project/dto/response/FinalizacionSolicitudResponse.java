package org.ecotrack_project.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinalizacionSolicitudResponse {

    private String estado;
    private LocalDateTime fechaFinalizacion;
    private String observacion;
}