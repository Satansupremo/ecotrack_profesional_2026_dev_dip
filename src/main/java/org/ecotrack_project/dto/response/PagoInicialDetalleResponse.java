
package org.ecotrack_project.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PagoInicialDetalleResponse {

    private int idSolicitud;
    private String tituloSolicitud;
    private LocalDateTime fechaRegistroSolicitud;

    private int idAsignacionInicial;
    private BigDecimal montoAsignadoInicial;
    private LocalDateTime fechaRegistroAsignacion;

    private int idPagoInicial;
    private BigDecimal montoPago;
    private LocalTime horaOperacion;
    private String numeroOperacion;
    private String estadoPago;
    private LocalDate fechaOperacion;

    private String nombreMedioPago;
}
