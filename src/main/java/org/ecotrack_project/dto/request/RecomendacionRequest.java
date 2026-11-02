package org.ecotrack_project.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RecomendacionRequest {

    @NotNull(message = "La cantidad de unidades es obligatoria")
    @Min(value = 1, message = "La cantidad de unidades debe ser mayor a cero")
    private Integer cantidadUnidades;

    @NotBlank(message = "La razón de las unidades recomendadas es obligatoria")
    private String razonCantidadUnidades;

    @NotBlank(message = "El equipo de seguridad recomendado es obligatorio")
    private String equipoSeguridad;

    private String otrasRecomendaciones;
}