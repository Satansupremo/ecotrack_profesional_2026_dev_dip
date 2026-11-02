package org.ecotrack_project.enums;

public enum SituacionVehiculo {
    DISPONIBLE,
    ASIGNADO,
    MANTENIMIENTO,
    RESERVADO,
    INACTIVO
}

/*
DISPONIBLE: puede utilizarse en una programación.
ASIGNADO: está reservado para una programación.
MANTENIMIENTO: no puede utilizarse por reparación o revisión técnica.
INACTIVO: está fuera de servicio o ya no pertenece a la flota operativa.
*/