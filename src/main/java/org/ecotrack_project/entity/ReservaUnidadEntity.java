
package org.ecotrack_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(
        name = "tb_reserva_unidad",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reserva_vehiculo",
                        columnNames = "id_vehiculo"
                ),
                @UniqueConstraint(
                        name = "uk_reserva_transportista",
                        columnNames = "id_transportista"
                )
        }
)
public class ReservaUnidadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private int idReserva;

    @Column(name = "fecha_reserva", nullable = false)
    private LocalDateTime fechaReserva;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_programacion", nullable = false)
    private ProgramacionEntity programacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_transportista", nullable = false)
    private UsuarioEntity transportista;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_vehiculo", nullable = false)
    private VehiculoEntity vehiculo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_reserva", nullable = false)
    private UsuarioEntity usuarioReserva;
}
