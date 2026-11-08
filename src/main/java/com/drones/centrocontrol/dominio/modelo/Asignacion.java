package com.drones.centrocontrol.dominio.modelo;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Vínculo entre una misión y el dron elegido. Guarda una instantánea de los datos
 * que justificaron la decisión (HU-08) para que cambios posteriores del dron no
 * alteren la explicación histórica (HU-12).
 */
public class Asignacion {

    private final String id;
    private final Mision mision;
    private final Dron dron;
    private final String estrategia;
    private final LocalDateTime fecha;
    private final double distanciaAlDestinoKm;
    private final double bateriaAlAsignar;
    private boolean activa;

    public Asignacion(String id, Mision mision, Dron dron, String estrategia, LocalDateTime fecha,
                      double distanciaAlDestinoKm, double bateriaAlAsignar, boolean activa) {
        this.id = Objects.requireNonNull(id, "El identificador de la asignación es obligatorio");
        this.mision = Objects.requireNonNull(mision, "La misión es obligatoria");
        this.dron = Objects.requireNonNull(dron, "El dron es obligatorio");
        this.estrategia = Objects.requireNonNull(estrategia, "La estrategia es obligatoria");
        this.fecha = Objects.requireNonNull(fecha, "La fecha es obligatoria");
        this.distanciaAlDestinoKm = distanciaAlDestinoKm;
        this.bateriaAlAsignar = bateriaAlAsignar;
        this.activa = activa;
    }

    /** La asignación deja de estar activa cuando la misión se completa o se cancela. */
    public void cerrar() {
        activa = false;
    }

    public String getId() {
        return id;
    }

    public Mision getMision() {
        return mision;
    }

    public Dron getDron() {
        return dron;
    }

    public String getEstrategia() {
        return estrategia;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public double getDistanciaAlDestinoKm() {
        return distanciaAlDestinoKm;
    }

    public double getBateriaAlAsignar() {
        return bateriaAlAsignar;
    }

    public boolean isActiva() {
        return activa;
    }
}
