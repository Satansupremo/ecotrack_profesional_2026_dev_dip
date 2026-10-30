package org.ecotrack_project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ecotrack_project.enums.EstadoProgramacion;
import org.ecotrack_project.enums.TipoVehiculo;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tb_programacion")
@Builder
public class ProgramacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_programacion")
    private int idProgramacion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_ejecucion")
    private LocalDateTime fechaEjecucion;

    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_solicitud", nullable = false)
    private SolicitudEntity solicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_coordinador", nullable = false)
    private UsuarioEntity coordinador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_jefe_unidad", nullable = false)
    private UsuarioEntity jefeUnidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoProgramacion estado;
}
