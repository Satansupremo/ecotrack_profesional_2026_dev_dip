package org.ecotrack_project.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tb_medio_pago")
@Builder
public class MedioPagoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_medio_pago")
    private int idMedioPago;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "numero_cuenta")
    private String numeroCuenta;

    @Column(name = "estado", nullable = false)
    private boolean estado;
}
