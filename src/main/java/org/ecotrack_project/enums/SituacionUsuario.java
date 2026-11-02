package org.ecotrack_project.enums;

public enum SituacionUsuario {
    NINGUNO,
    DISPONIBLE,
    ASIGNADO,
    RESERVADO,
    INACTIVO
}

/*
NINGUNO: estado del usuario que no es un trabajador del negocio
DISPONIBLE: puede ser asignado a una programación.
ASIGNADO: está comprometido con una programación.
INACTIVO: no puede ser asignado temporalmente o ha sido deshabilitado.
*/
