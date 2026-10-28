package org.ecotrack_project.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tb_articulo_recuperado")
@Builder
public class ArticuloRecuperadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_articulo_recuperado")
    private int idArticuloRecuperado;

    @Column(name = "articulo", nullable = false)
    private String articulo;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_carga", nullable = false)
    private CargaEntity carga;
}
