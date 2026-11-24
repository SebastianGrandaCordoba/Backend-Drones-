package com.drones.centrocontrol.dominio.modelo;

import java.util.List;

/**
 * Resultado de intentar asignar una misión: la asignación creada o, si no hubo
 * candidato elegible, los motivos que lo impidieron (HU-09). No encontrar dron
 * es un resultado válido del negocio, no un error técnico.
 */
public class ResultadoAsignacion {

    private Asignacion asignacion;
    private List<MotivoRechazo> motivos;

    public ResultadoAsignacion(Asignacion asignacion, List<MotivoRechazo> motivos) {
        this.asignacion = asignacion;
        this.motivos = motivos == null ? List.of() : List.copyOf(motivos);
    }

    public boolean fueExitoso() {
        return asignacion != null;
    }

    public Asignacion getAsignacion() {
        return asignacion;
    }

    public List<MotivoRechazo> getMotivos() {
        return motivos;
    }

    public void setAsignacion(Asignacion asignacion) {
        this.asignacion = asignacion;
    }

    public void setMotivos(List<MotivoRechazo> motivos) {
        this.motivos = motivos == null ? List.of() : List.copyOf(motivos);
    }
}
