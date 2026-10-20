package org.ecotrack_project.dto.response;

import lombok.*;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehiculoResponse {

    private int idVehiculo;
    private Date fechaRegistro;
    private String placa;
    private String marca;
    private String modelo;
    private Float capacidadLibras;
    private boolean estado;
}
