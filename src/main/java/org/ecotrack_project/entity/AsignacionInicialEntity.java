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
@Table(name = "tb_asignacion_inicial")
@Builder
public class AsignacionInicialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignacion_inicial")
    private int idAsignacionInicial;

    @Column(name = "monto_asignado_inicial", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoAsignadoInicial;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_solicitud", nullable = false)
    private SolicitudEntity solicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_coordinador", nullable = false)
    private UsuarioEntity coordinador;
}
