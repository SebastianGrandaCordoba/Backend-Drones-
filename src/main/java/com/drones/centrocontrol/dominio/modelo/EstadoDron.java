package com.drones.centrocontrol.dominio.modelo;

/** Estados operativos de un dron. Solo un dron DISPONIBLE puede recibir una misión. */
public enum EstadoDron {
    DISPONIBLE, ASIGNADO, EN_MISION, MANTENIMIENTO
}
