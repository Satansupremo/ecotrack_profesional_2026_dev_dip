package org.ecotrack_project.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SolicitudResponse {

    private int idSolicitud;
    private String titulo;
    private String descripcion;
    private String ubicacion;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaFinalizacion;
    private String estado;
}