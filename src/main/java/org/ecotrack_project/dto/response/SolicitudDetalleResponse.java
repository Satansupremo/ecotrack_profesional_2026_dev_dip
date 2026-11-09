
package org.ecotrack_project.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SolicitudDetalleResponse {

    private Integer idSolicitud;
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

    private Integer cantidadUnidades;
    private String razonCantidadUnidades;

    private List<FaseSolicitudResponse> fases;

    private PagoInicialResponse pagoInicial;
    private VerificacionSolicitudResponse verificacion;
    private ProgramacionDetalleResponse programacion;
    private EjecucionRecojoResponse ejecucion;
    private PagoTotalResponse pagoTotal;
    private FinalizacionSolicitudResponse finalizacion;

    private String equipoSeguridad;
    private String otrasRecomendaciones;
}


