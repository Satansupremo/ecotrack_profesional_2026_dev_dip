package org.ecotrack_project.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColaResponse {

    private Integer idCola;
    private LocalDateTime fechaRegistro;
    private Boolean estado;

    private Integer idProgramacion;
    private Integer idDetalle;

    private Integer idTransportista;
    private String nombreTransportista;
    private String apellidoTransportista;

    private Integer idVehiculo;
    private String placaVehiculo;
    private String tipoVehiculo;
}