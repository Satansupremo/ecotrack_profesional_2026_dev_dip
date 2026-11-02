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

    @Column(name = "cantidad_unidades", nullable = false)
    private int cantidadUnidades;

    @Column(name = "razon_cantidad_unidades", columnDefinition = "TEXT")
    private String razonCantidadUnidades;

    @Column(name = "equipo_seguridad", columnDefinition = "TEXT")
    private String equipoSeguridad;

    @Column(name = "otras_recomendaciones", columnDefinition = "TEXT")
    private String otrasRecomendaciones;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_solicitud", nullable = false, unique = true)
    private SolicitudEntity solicitud;
}
