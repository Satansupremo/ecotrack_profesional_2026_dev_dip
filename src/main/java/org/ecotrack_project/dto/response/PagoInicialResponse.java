package org.ecotrack_project.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PagoInicialResponse {

    private String estado;
    private BigDecimal monto;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaConfirmacion;
}
