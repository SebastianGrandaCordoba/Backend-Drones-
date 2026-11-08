package com.drones.centrocontrol.dominio.modelo;

/** Dron de fotografía: su capacidad mide almacenamiento de imágenes en GB. */
public class DronFotografia extends Dron {

    public DronFotografia(String id, double bateria, double autonomiaKm, double capacidad,
                  EstadoDron estado, Ubicacion ubicacion) {
        super(id, bateria, autonomiaKm, capacidad, estado, ubicacion);
    }

    @Override
    public TipoMision getTipo() {
        return TipoMision.FOTOGRAFIA;
    }

    @Override
    public String getUnidadCapacidad() {
        return "GB";
    }
}
