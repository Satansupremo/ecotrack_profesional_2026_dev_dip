package org.ecotrack_project.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tb_recomendacion")
@Builder
public class RecomendacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recomendacion")
    private int idRecomendacion;

    @Column(name = "recomendacion_equipo", columnDefinition = "TEXT")
    private String recomendacionEquipo;

    @Column(name = "recomendacion_vehiculo", columnDefinition = "TEXT")
    private String recomendacionVehiculo;

    @Column(name = "recomendacion_otro", columnDefinition = "TEXT")
    private String recomendacionOtro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_programacion", nullable = false)
    private ProgramacionEntity programacion;
}
