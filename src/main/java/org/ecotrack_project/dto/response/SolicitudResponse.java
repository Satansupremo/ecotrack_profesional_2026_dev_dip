package org.ecotrack_project.dto.response;

import lombok.*;

import java.time.LocalDateTime;

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

    private String nombreUsuario;
    private String apellidoUsuario;
    private String correo;
    private String celular;

    private Integer idCoordinador;
    private String nombreCoordinador;
    private boolean asignadaAlCoordinadorActual;

    private Integer cantidadUnidades;
    private String razonCantidadUnidades;
}