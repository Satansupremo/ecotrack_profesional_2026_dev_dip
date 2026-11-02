
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

    @Column(name = "monto_asignado_total", nullable = false)
    private BigDecimal montoAsignadoTotal;

    @Column(name = "razon", nullable = false)
    private String razon;

    @Column(name = "monto_descontado", nullable = false)
    private BigDecimal montoDescontado;

    @Column(name = "monto_por_cobrar", nullable = false)
    private BigDecimal montoPorCobrar;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_solicitud", nullable = false, unique = true)
    private SolicitudEntity solicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_coordinador", nullable = false)
    private UsuarioEntity coordinador;
}