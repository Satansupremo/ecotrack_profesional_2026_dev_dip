package org.ecotrack_project.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedioPagoResponse {

    private int idMedioPago;
    private String nombre;
    private String descripcion;
    private String numeroCuenta;
    private boolean estado;
}
