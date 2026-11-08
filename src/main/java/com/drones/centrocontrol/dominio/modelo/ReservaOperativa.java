package com.drones.centrocontrol.dominio.modelo;

/** Margen de autonomía (km) que debe sobrar después de cubrir la distancia de una misión (HU-13). */
public class ReservaOperativa {

    private final double km;

    public ReservaOperativa(double km) {
        if (km < 0) {
            throw new IllegalArgumentException("La reserva operativa no puede ser negativa");
        }
        this.km = km;
    }

    /** Indica si la autonomía alcanza para la distancia más la reserva. */
    public boolean cubre(double autonomiaKm, double distanciaKm) {
        return autonomiaKm >= distanciaKm + km;
    }

    public double getKm() {
        return km;
    }
}
