package com.drones.centrocontrol.dominio.modelo;

import java.util.Objects;

/**
 * Dron de la flota. Es abstracto porque la capacidad significa algo distinto en cada tipo
 * (kg, GB, h, L o ha); cada subclase define su tipo de misión y su unidad.
 * El estado solo cambia mediante operaciones con significado de negocio (encapsulamiento).
 */
public abstract class Dron {

    private final String id;
    protected double bateria;
    protected double autonomiaKm;
    protected double capacidad;
    protected EstadoDron estado;
    protected Ubicacion ubicacion;

    protected Dron(String id, double bateria, double autonomiaKm, double capacidad,
                   EstadoDron estado, Ubicacion ubicacion) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El identificador del dron es obligatorio");
        }
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que cero");
        }
        validarBateria(bateria);
        validarAutonomia(autonomiaKm);
        this.id = id;
        this.bateria = bateria;
        this.autonomiaKm = autonomiaKm;
        this.capacidad = capacidad;
        this.estado = Objects.requireNonNull(estado, "El estado del dron es obligatorio");
        this.ubicacion = Objects.requireNonNull(ubicacion, "La ubicación del dron es obligatoria");
    }

    /** Tipo de misión que este dron puede atender. */
    public abstract TipoMision getTipo();

    /** Unidad en la que se expresa la capacidad de este tipo de dron. */
    public abstract String getUnidadCapacidad();

    /**
     * Regla de elegibilidad (HU-06): disponible, mismo tipo, capacidad suficiente
     * y autonomía que cubra la distancia más la reserva operativa.
     */
    public boolean puedeAtender(Mision mision, ReservaOperativa reserva) {
        return estado == EstadoDron.DISPONIBLE
                && getTipo() == mision.getTipo()
                && capacidad >= mision.getCapacidadRequerida()
                && reserva.cubre(autonomiaKm, mision.getDistanciaKm());
    }

    /** Actualización manual de datos operativos (HU-03). */
    public void actualizarDatosOperativos(double bateria, double autonomiaKm, Ubicacion ubicacion) {
        validarBateria(bateria);
        validarAutonomia(autonomiaKm);
        this.bateria = bateria;
        this.autonomiaKm = autonomiaKm;
        this.ubicacion = Objects.requireNonNull(ubicacion, "La ubicación del dron es obligatoria");
    }

    /** DISPONIBLE → ASIGNADO (HU-06). */
    public void marcarAsignado() {
        exigirEstado(EstadoDron.DISPONIBLE, "asignar");
        estado = EstadoDron.ASIGNADO;
    }

    /** ASIGNADO → EN_MISION (HU-11). */
    public void iniciarMision() {
        exigirEstado(EstadoDron.ASIGNADO, "iniciar la misión de");
        estado = EstadoDron.EN_MISION;
    }

    /** Vuelve a DISPONIBLE al terminar o cancelar su misión, o al salir de mantenimiento. */
    public void liberar() {
        estado = EstadoDron.DISPONIBLE;
    }

    /** DISPONIBLE → MANTENIMIENTO (HU-03). Un dron con misión activa no puede ir a mantenimiento. */
    public void enviarAMantenimiento() {
        if (estado == EstadoDron.MANTENIMIENTO) {
            return;
        }
        exigirEstado(EstadoDron.DISPONIBLE, "enviar a mantenimiento");
        estado = EstadoDron.MANTENIMIENTO;
    }

    private void exigirEstado(EstadoDron esperado, String accion) {
        if (estado != esperado) {
            throw new IllegalStateException("No se puede " + accion + " el dron " + id
                    + " porque está " + estado + " (se requiere " + esperado + ")");
        }
    }

    private static void validarBateria(double bateria) {
        if (bateria < 0 || bateria > 100) {
            throw new IllegalArgumentException("La batería debe estar entre 0 y 100 %");
        }
    }

    private static void validarAutonomia(double autonomiaKm) {
        if (autonomiaKm <= 0) {
            throw new IllegalArgumentException("La autonomía debe ser mayor que cero");
        }
    }

    public String getId() {
        return id;
    }

    public double getBateria() {
        return bateria;
    }

    public double getAutonomiaKm() {
        return autonomiaKm;
    }

    public double getCapacidad() {
        return capacidad;
    }

    public EstadoDron getEstado() {
        return estado;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }
}
