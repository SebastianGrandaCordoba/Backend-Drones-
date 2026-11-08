package com.drones.centrocontrol.dominio.modelo;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Misión que debe atender un dron. El estado solo avanza por las transiciones
 * permitidas del ciclo de vida (HU-11); no se modifica directamente desde fuera.
 */
public class Mision {

    private final String id;
    private final TipoMision tipo;
    private final Prioridad prioridad;
    private final double distanciaKm;
    private final LocalDateTime limiteTiempo;
    private final Ubicacion destino;
    private final double capacidadRequerida;
    private EstadoMision estado;

    public Mision(String id, TipoMision tipo, Prioridad prioridad, double distanciaKm,
                  LocalDateTime limiteTiempo, Ubicacion destino, double capacidadRequerida,
                  EstadoMision estado) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El identificador de la misión es obligatorio");
        }
        if (distanciaKm <= 0) {
            throw new IllegalArgumentException("La distancia planificada debe ser mayor que cero");
        }
        if (capacidadRequerida < 0) {
            throw new IllegalArgumentException("La capacidad requerida no puede ser negativa");
        }
        this.id = id;
        this.tipo = Objects.requireNonNull(tipo, "El tipo de misión es obligatorio");
        this.prioridad = Objects.requireNonNull(prioridad, "La prioridad es obligatoria");
        this.distanciaKm = distanciaKm;
        this.limiteTiempo = Objects.requireNonNull(limiteTiempo, "El límite de tiempo es obligatorio");
        this.destino = Objects.requireNonNull(destino, "El destino es obligatorio");
        this.capacidadRequerida = capacidadRequerida;
        this.estado = Objects.requireNonNull(estado, "El estado de la misión es obligatorio");
    }

    public boolean estaPendiente() {
        return estado == EstadoMision.PENDIENTE;
    }

    /** PENDIENTE → ASIGNADA (HU-06). */
    public void marcarAsignada() {
        exigirEstado(EstadoMision.PENDIENTE, "asignar");
        estado = EstadoMision.ASIGNADA;
    }

    /** ASIGNADA → EN_EJECUCION (HU-11). */
    public void iniciar() {
        exigirEstado(EstadoMision.ASIGNADA, "iniciar");
        estado = EstadoMision.EN_EJECUCION;
    }

    /** EN_EJECUCION → COMPLETADA (HU-11). */
    public void completar() {
        exigirEstado(EstadoMision.EN_EJECUCION, "completar");
        estado = EstadoMision.COMPLETADA;
    }

    /** PENDIENTE o ASIGNADA → CANCELADA. Una misión en ejecución no se cancela (fuera de alcance). */
    public void cancelar() {
        if (estado != EstadoMision.PENDIENTE && estado != EstadoMision.ASIGNADA) {
            throw new IllegalStateException("No se puede cancelar la misión " + id + " porque está " + estado
                    + " (solo se cancelan misiones PENDIENTE o ASIGNADA)");
        }
        estado = EstadoMision.CANCELADA;
    }

    private void exigirEstado(EstadoMision esperado, String accion) {
        if (estado != esperado) {
            throw new IllegalStateException("No se puede " + accion + " la misión " + id
                    + " porque está " + estado + " (se requiere " + esperado + ")");
        }
    }

    public String getId() {
        return id;
    }

    public TipoMision getTipo() {
        return tipo;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public double getDistanciaKm() {
        return distanciaKm;
    }

    public LocalDateTime getLimiteTiempo() {
        return limiteTiempo;
    }

    public Ubicacion getDestino() {
        return destino;
    }

    public double getCapacidadRequerida() {
        return capacidadRequerida;
    }

    public EstadoMision getEstado() {
        return estado;
    }
}
