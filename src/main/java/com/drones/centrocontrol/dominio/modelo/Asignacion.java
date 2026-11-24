package com.drones.centrocontrol.dominio.modelo;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Vínculo entre una misión y el dron elegido. Guarda una instantánea de los datos
 * que justificaron la decisión (HU-08) para que cambios posteriores del dron no
 * alteren la explicación histórica (HU-12).
 */
public class Asignacion {

    private String id;
    private Mision mision;
    private Dron dron;
    private String estrategia;
    private LocalDateTime fecha;
    private double distanciaAlDestinoKm;
    private double bateriaAlAsignar;
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

    // Setters: aplican las mismas validaciones del constructor para no romper invariantes.

    public void setId(String id) {
        this.id = Objects.requireNonNull(id, "El identificador de la asignación es obligatorio");
    }

    public void setMision(Mision mision) {
        this.mision = Objects.requireNonNull(mision, "La misión es obligatoria");
    }

    public void setDron(Dron dron) {
        this.dron = Objects.requireNonNull(dron, "El dron es obligatorio");
    }

    public void setEstrategia(String estrategia) {
        this.estrategia = Objects.requireNonNull(estrategia, "La estrategia es obligatoria");
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = Objects.requireNonNull(fecha, "La fecha es obligatoria");
    }

    public void setDistanciaAlDestinoKm(double distanciaAlDestinoKm) {
        this.distanciaAlDestinoKm = distanciaAlDestinoKm;
    }

    public void setBateriaAlAsignar(double bateriaAlAsignar) {
        this.bateriaAlAsignar = bateriaAlAsignar;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}
