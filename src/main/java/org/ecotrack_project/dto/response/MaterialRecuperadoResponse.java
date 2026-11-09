package org.ecotrack_project.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MaterialRecuperadoResponse {

    private Integer idArticuloRecuperado;
    private String articulo;
    private Integer cantidad;
}