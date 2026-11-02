package org.ecotrack_project.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class PagoInicialRequest {

    @NotNull(message = "El medio de pago es obligatorio")
    private Integer idMedioPago;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero")
    private BigDecimal monto;

    @NotNull(message = "La fecha de operación es obligatoria")
    private LocalDate fechaOperacion;

    @NotNull(message = "La hora de operación es obligatoria")
    private LocalTime horaOperacion;

    @NotBlank(message = "El número de operación es obligatorio")
    private String numeroOperacion;
}