package org.ecotrack_project.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramacionResponse {

    private Integer idProgramacion;
    private LocalDateTime fechaRegistro;
    private LocalDate fechaEjecucion;
    private String estado;

    private Integer idSolicitud;
    private String tituloSolicitud;
    private String descripcionSolicitud;
    private String ubicacionSolicitud;
    private LocalDateTime fechaRegistroSolicitud;

    private Integer cantidadUnidades;
    private Integer cantidadSalidasPendientes;
    private String razonCantidadUnidades;
    private String equipoSeguridad;
    private String otrasRecomendaciones;

    private Integer idJefeUnidad;
    private String nombreJefeUnidad;
    private String apellidoJefeUnidad;
}