package com.drones.centrocontrol.dominio.modelo;

/** Ciclo de vida de una misión (HU-04, HU-06, HU-11). */
public enum EstadoMision {
    PENDIENTE, ASIGNADA, EN_EJECUCION, COMPLETADA, CANCELADA
}
