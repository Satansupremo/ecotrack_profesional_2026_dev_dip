package org.ecotrack_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity(name = "tb_vehiculo")
@Table
@Builder
public class VehiculoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehiculo")
    private int idVehiculo;
    @Column(name = "fecha_registro")
    private Date fechaRegistro;
    @Column(name = "placa")
    private String placa;
    @Column(name = "marca")
    private String marca;
    @Column(name = "modelo")
    private String modelo;
    @Column(name = "capacidad_libras")
    private Float capacidadLibras;
    @Column(name = "estado")
    private boolean estado;
}
