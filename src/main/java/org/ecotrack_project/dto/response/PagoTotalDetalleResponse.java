package org.ecotrack_project.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
public class PagoTotalDetalleResponse {

    private int idSolicitud;
    private String tituloSolicitud;
    private LocalDateTime fechaRegistroSolicitud;

    private int idAsignacionTotal;
    private BigDecimal montoAsignadoTotal;
    private BigDecimal montoDescontado;
    private BigDecimal montoPorCobrar;
    private LocalDateTime fechaRegistroAsignacion;

    private int idPagoTotal;
    private BigDecimal montoPago;
    private LocalDate fechaOperacion;
    private LocalTime horaOperacion;
    private String numeroOperacion;
    private String estadoPago;
    private String nombreMedioPago;
}