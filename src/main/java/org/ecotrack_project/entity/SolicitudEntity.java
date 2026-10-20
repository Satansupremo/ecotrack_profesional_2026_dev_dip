package org.ecotrack_project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ecotrack_project.enums.SolicitudEstado;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tb_solicitud")
@Builder
public class SolicitudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private int idSolicitud;
    @Column(name = "fecha_registro", nullable = false)
    private Date fechaRegistro;
    @Column(name = "titulo", nullable = false)
    private String titulo;
    @Column(name = "descripcion", nullable = false)
    private String descripcion;
    @Column(name = "ubicacion", nullable = false)
    private String ubicacion;
    @Column(name = "fecha_finalizacion")
    private Date fechaFinalizacion;
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private SolicitudEstado estado;
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;
    @ManyToOne
    @JoinColumn(name = "id_coordinador")
    private UsuarioEntity coordinador;
}
