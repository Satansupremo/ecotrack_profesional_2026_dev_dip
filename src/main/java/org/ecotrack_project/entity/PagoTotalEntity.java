package org.ecotrack_project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ecotrack_project.enums.EstadoPagoTotal;
import org.ecotrack_project.enums.TipoVehiculo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tb_pago_total")
@Builder
public class PagoTotalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago_total")
    private int idPagoTotal;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_operacion", nullable = false)
    private LocalDate fechaOperacion;

    @Column(name = "hora_operacion", nullable = false)
    private LocalTime horaOperacion;

    @Column(name = "numero_operacion", nullable = false)
    private String numeroOperacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPagoTotal estado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_medio_pago", nullable = false)
    private MedioPagoEntity medioPago;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_solicitud", nullable = false)
    private SolicitudEntity solicitud;
}
