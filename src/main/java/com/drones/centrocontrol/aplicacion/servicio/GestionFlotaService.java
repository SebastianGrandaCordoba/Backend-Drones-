package com.drones.centrocontrol.aplicacion.servicio;

import com.drones.centrocontrol.aplicacion.puerto.entrada.IConsultaFlota;
import com.drones.centrocontrol.aplicacion.puerto.entrada.IGestionFlota;
import com.drones.centrocontrol.aplicacion.puerto.salida.DronRepositorio;
import com.drones.centrocontrol.dominio.modelo.Dron;
import com.drones.centrocontrol.dominio.modelo.DronAgricultura;
import com.drones.centrocontrol.dominio.modelo.DronCartografia;
import com.drones.centrocontrol.dominio.modelo.DronEntrega;
import com.drones.centrocontrol.dominio.modelo.DronFotografia;
import com.drones.centrocontrol.dominio.modelo.DronVigilancia;
import com.drones.centrocontrol.dominio.modelo.EstadoDron;
import com.drones.centrocontrol.dominio.modelo.TipoMision;
import com.drones.centrocontrol.dominio.modelo.Ubicacion;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/** Componente «Gestión de Flota»: registra, actualiza y consulta drones. */
public class GestionFlotaService implements IGestionFlota, IConsultaFlota {

    private final DronRepositorio dronRepositorio;

    public GestionFlotaService(DronRepositorio dronRepositorio) {
        this.dronRepositorio = dronRepositorio;
    }

    @Override
    public Dron registrar(String id, TipoMision tipo, double bateria, double autonomiaKm, double capacidad,
                          EstadoDron estadoInicial, Ubicacion ubicacion) {
        Objects.requireNonNull(tipo, "El tipo de dron es obligatorio");
        if (estadoInicial != EstadoDron.DISPONIBLE && estadoInicial != EstadoDron.MANTENIMIENTO) {
            throw new IllegalArgumentException("El estado inicial debe ser DISPONIBLE o MANTENIMIENTO");
        }
        if (id != null && dronRepositorio.existe(id)) {
            throw new IllegalStateException("Ya existe un dron con el identificador " + id);
        }
        Dron dron = switch (tipo) {
            case ENTREGA -> new DronEntrega(id, bateria, autonomiaKm, capacidad, estadoInicial, ubicacion);
            case FOTOGRAFIA -> new DronFotografia(id, bateria, autonomiaKm, capacidad, estadoInicial, ubicacion);
            case VIGILANCIA -> new DronVigilancia(id, bateria, autonomiaKm, capacidad, estadoInicial, ubicacion);
            case AGRICULTURA -> new DronAgricultura(id, bateria, autonomiaKm, capacidad, estadoInicial, ubicacion);
            case CARTOGRAFIA -> new DronCartografia(id, bateria, autonomiaKm, capacidad, estadoInicial, ubicacion);
        };
        return dronRepositorio.guardar(dron);
    }

    @Override
    public Dron actualizarDatosOperativos(String id, double bateria, double autonomiaKm, Ubicacion ubicacion) {
        Dron dron = buscarPorId(id);
        dron.actualizarDatosOperativos(bateria, autonomiaKm, ubicacion);
        return dronRepositorio.guardar(dron);
    }

    @Override
    public Dron cambiarEstado(String id, EstadoDron nuevoEstado) {
        Dron dron = buscarPorId(id);
        if (nuevoEstado == EstadoDron.MANTENIMIENTO) {
            dron.enviarAMantenimiento();
        } else if (nuevoEstado == EstadoDron.DISPONIBLE) {
            if (dron.getEstado() == EstadoDron.ASIGNADO || dron.getEstado() == EstadoDron.EN_MISION) {
                throw new IllegalStateException("El dron " + id + " tiene una misión activa; se libera al completar o "
                        + "cancelar la misión");
            }
            dron.liberar();
        } else {
            throw new IllegalArgumentException("El operador solo puede cambiar el estado a DISPONIBLE o MANTENIMIENTO");
        }
        return dronRepositorio.guardar(dron);
    }

    @Override
    public List<Dron> consultar(TipoMision tipo, EstadoDron estado) {
        return dronRepositorio.buscar(tipo, estado);
    }

    @Override
    public Dron buscarPorId(String id) {
        return dronRepositorio.buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró el dron " + id));
    }
}
