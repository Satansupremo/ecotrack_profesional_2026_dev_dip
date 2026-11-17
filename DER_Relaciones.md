# DER — Relaciones

Diagrama de las relaciones JPA entre las 17 entidades del proyecto, sin atributos.
Las cardinalidades reflejan las claves foráneas, restricciones `unique` y nulabilidad
declaradas en las entidades.

```mermaid
erDiagram
    UsuarioEntity ||--o{ SolicitudEntity : "presenta"

    SolicitudEntity ||--o{ ProgramacionEntity : "origina"
    UsuarioEntity ||--o{ ProgramacionEntity : "coordina"
    UsuarioEntity ||--o{ ProgramacionEntity : "es jefe de unidad"

    SolicitudEntity ||--o| AsignacionCoordinadorEntity : "tiene asignacion"
    UsuarioEntity ||--o| AsignacionCoordinadorEntity : "es coordinador asignado"

    SolicitudEntity ||--o| AsignacionInicialEntity : "tiene asignacion inicial"
    UsuarioEntity ||--o{ AsignacionInicialEntity : "registra como coordinador"

    SolicitudEntity ||--o| AsignacionTotalEntity : "tiene asignacion total"
    UsuarioEntity ||--o{ AsignacionTotalEntity : "registra como coordinador"

    SolicitudEntity ||--o| RecomendacionEntity : "tiene recomendacion"

    SolicitudEntity ||--o{ PagoInicialEntity : "recibe"
    MedioPagoEntity ||--o{ PagoInicialEntity : "se usa en"
    SolicitudEntity ||--o{ PagoTotalEntity : "recibe"
    MedioPagoEntity ||--o{ PagoTotalEntity : "se usa en"

    UsuarioEntity ||--o{ NotificacionEntity : "recibe"

    ProgramacionEntity ||--o{ DetalleEntity : "incluye"
    UsuarioEntity ||--o{ DetalleEntity : "transporta en"
    VehiculoEntity ||--o{ DetalleEntity : "se asigna en"

    DetalleEntity ||--o{ ColaEntity : "genera"
    ColaEntity ||--o| CargaEntity : "registra carga"
    ProgramacionEntity ||--o{ CargaEntity : "incluye"
    UsuarioEntity ||--o{ CargaEntity : "registra como operario"
    UsuarioEntity ||--o{ CargaEntity : "transporta en"
    CargaEntity ||--o{ ArticuloRecuperadoEntity : "registra articulos"

    ProgramacionEntity ||--o{ ReservaUnidadEntity : "tiene reservas"
    UsuarioEntity ||--o{ ReservaUnidadEntity : "realiza reserva"
    UsuarioEntity ||--o| ReservaUnidadEntity : "se reserva como transportista"
    VehiculoEntity ||--o| ReservaUnidadEntity : "se reserva"
```

`o|` indica cero o una relación; `o{` indica cero o muchas. Las relaciones
obligatorias desde las entidades dependientes están representadas por `||`.
