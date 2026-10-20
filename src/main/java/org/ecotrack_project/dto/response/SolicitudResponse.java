package org.ecotrack_project.dto.response;

import lombok.*;

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
    private Date fechaRegistro;
    private Date fechaFinalizacion;
    private String estado;
}