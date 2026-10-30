package org.ecotrack_project.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MedioPagoResponse {

    private Integer idMedioPago;
    private String nombre;
    private String descripcion;
    private String numeroCuenta;
}