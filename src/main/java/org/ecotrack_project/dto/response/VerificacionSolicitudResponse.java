package org.ecotrack_project.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerificacionSolicitudResponse {

    private String estado;
    private String observacion;
    private LocalDateTime fechaVerificacion;
}