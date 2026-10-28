package org.ecotrack_project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehiculoRequest {

    @NotBlank(message = "La placa es obligatoria")
    private String placa;
    @NotBlank(message = "La marca es obligatoria")
    private String marca;
    @NotBlank(message = "El modelo es obligatorio")
    private String modelo;
    @NotNull(message = "La capacidad en libras es obligatoria")
    @Positive(message = "La capacidad debe ser un numero mayor a cero")
    private BigDecimal capacidadLibras;
}
