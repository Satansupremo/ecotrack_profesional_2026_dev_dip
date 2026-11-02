package org.ecotrack_project.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class UnidadProgramacionResponse {

    private int idDetalle;
    private int idTransportista;
    private String nombreTransportista;
    private String apellidoTransportista;
    private String celular;
    private int idVehiculo;
    private String placa;
    private String marca;
    private String modelo;
    private String tipoVehiculo;
    private BigDecimal capacidadLibras;
}