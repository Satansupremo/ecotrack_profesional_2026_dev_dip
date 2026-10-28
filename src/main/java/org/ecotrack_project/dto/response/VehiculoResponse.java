package org.ecotrack_project.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehiculoResponse {

    private int idVehiculo;
    private LocalDateTime fechaRegistro;
    private String placa;
    private String marca;
    private String modelo;
    private BigDecimal capacidadLibras;
    private boolean estado;
}
