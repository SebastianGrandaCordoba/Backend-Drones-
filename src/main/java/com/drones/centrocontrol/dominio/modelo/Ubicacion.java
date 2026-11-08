package com.drones.centrocontrol.dominio.modelo;

/**
 * Coordenadas geográficas de un dron o del destino de una misión.
 * Es inmutable: un cambio de posición se representa con una nueva ubicación.
 */
public class Ubicacion {

    private static final double RADIO_TIERRA_KM = 6371.0;

    private final double latitud;
    private final double longitud;

    public Ubicacion(double latitud, double longitud) {
        if (latitud < -90 || latitud > 90) {
            throw new IllegalArgumentException("La latitud debe estar entre -90 y 90");
        }
        if (longitud < -180 || longitud > 180) {
            throw new IllegalArgumentException("La longitud debe estar entre -180 y 180");
        }
        this.latitud = latitud;
        this.longitud = longitud;
    }

    /**
     * Distancia geométrica (fórmula de Haversine) hasta otra ubicación, en km.
     * Es una aproximación: no considera rutas, obstáculos ni clima.
     */
    public double distanciaKmA(Ubicacion otra) {
        double dLat = Math.toRadians(otra.latitud - latitud);
        double dLon = Math.toRadians(otra.longitud - longitud);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(latitud)) * Math.cos(Math.toRadians(otra.latitud))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * RADIO_TIERRA_KM * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }
}
