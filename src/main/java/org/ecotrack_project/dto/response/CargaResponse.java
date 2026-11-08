
package org.ecotrack_project.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargaResponse {

    private Integer idCarga;
    private Integer idCola;
    private LocalDateTime fechaRegistro;
    private BigDecimal pesoLibras;
    private String estado;

    private Integer idProgramacion;
    private Integer idTransportista;
    private String nombreTransportista;
    private String apellidoTransportista;

    private Integer idOperario;
    private String nombreOperario;
    private String apellidoOperario;
}
