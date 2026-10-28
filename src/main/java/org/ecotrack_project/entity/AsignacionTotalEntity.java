package org.ecotrack_project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tb_asignacion_total")
@Builder
public class AsignacionTotalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignacion_total")
    private int idAsignacionTotal;

    @Column(name = "monto_asignado_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoAsignadoTotal;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_solicitud", nullable = false)
    private SolicitudEntity solicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_coordinador", nullable = false)
    private UsuarioEntity coordinador;
}
