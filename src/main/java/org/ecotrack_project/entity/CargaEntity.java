package org.ecotrack_project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ecotrack_project.enums.EstadoCarga;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tb_carga")
@Builder
public class CargaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carga")
    private int idCarga;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "peso_libras", nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoLibras;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_programacion", nullable = false)
    private ProgramacionEntity programacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_operario", nullable = false)
    private UsuarioEntity operario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_transportista", nullable = false)
    private UsuarioEntity transportista;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cola", nullable = false, unique = true)
    private ColaEntity cola;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    @Builder.Default
    private EstadoCarga estado = EstadoCarga.EN_PROCESO;
}
