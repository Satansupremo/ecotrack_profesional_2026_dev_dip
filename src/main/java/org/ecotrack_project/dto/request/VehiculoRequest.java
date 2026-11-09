package org.ecotrack_project.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.ecotrack_project.enums.TipoVehiculo;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoRequest {

    @NotBlank(message = "La placa es obligatoria")
    @Size(max = 20, message = "La placa no puede superar los 20 caracteres")
    private String placa;

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 50, message = "La marca no puede superar los 50 caracteres")
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 50, message = "El modelo no puede superar los 50 caracteres")
    private String modelo;

    @NotNull(message = "La capacidad en libras es obligatoria")
    @DecimalMin(value = "0.00", message = "La capacidad debe ser mayor o igual a 0")
    @Digits(integer = 8, fraction = 2, message = "Formato de capacidad inválido (máximo 8 enteros y 2 decimales)")
    private BigDecimal capacidadLibras;

    @NotNull(message = "El tipo de vehículo es obligatorio")
    private TipoVehiculo tipo;
}
