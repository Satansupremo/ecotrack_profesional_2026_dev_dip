package org.ecotrack_project.dto.response;

import lombok.*;
import org.ecotrack_project.enums.SituacionVehiculo;
import org.ecotrack_project.enums.TipoVehiculo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoResponse {

    private int idVehiculo;
    private LocalDateTime fechaRegistro;
    private String placa;
    private String marca;
    private String modelo;
    private BigDecimal capacidadLibras;
    private TipoVehiculo tipo;
    private boolean estado;
    private SituacionVehiculo situacion;
}
