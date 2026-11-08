package com.drones.centrocontrol.dominio;

import com.drones.centrocontrol.dominio.estrategia.MayorBateria;
import com.drones.centrocontrol.dominio.estrategia.MenorDistancia;
import com.drones.centrocontrol.dominio.modelo.Dron;
import com.drones.centrocontrol.dominio.modelo.DronEntrega;
import com.drones.centrocontrol.dominio.modelo.DronFotografia;
import com.drones.centrocontrol.dominio.modelo.EstadoDron;
import com.drones.centrocontrol.dominio.modelo.EstadoMision;
import com.drones.centrocontrol.dominio.modelo.Mision;
import com.drones.centrocontrol.dominio.modelo.MotivoRechazo;
import com.drones.centrocontrol.dominio.modelo.Prioridad;
import com.drones.centrocontrol.dominio.modelo.ReservaOperativa;
import com.drones.centrocontrol.dominio.modelo.ResultadoAsignacion;
import com.drones.centrocontrol.dominio.modelo.TipoMision;
import com.drones.centrocontrol.dominio.modelo.Ubicacion;
import com.drones.centrocontrol.dominio.servicio.MotorAsignacion;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MotorAsignacionTest {

    private static final Ubicacion DESTINO = new Ubicacion(6.2442, -75.5812);
    private static final ReservaOperativa RESERVA = new ReservaOperativa(5);

    private final MotorAsignacion motor = new MotorAsignacion(List.of(new MenorDistancia(), new MayorBateria()));

    private static Mision misionEntrega(double distanciaKm, double capacidadRequerida) {
        return new Mision("M1", TipoMision.ENTREGA, Prioridad.ALTA, distanciaKm, LocalDateTime.now().plusHours(2),
                DESTINO, capacidadRequerida, EstadoMision.PENDIENTE);
    }

    private static Dron entrega(String id, double bateria, double autonomia, double capacidad, double lat, double lon) {
        return new DronEntrega(id, bateria, autonomia, capacidad, EstadoDron.DISPONIBLE, new Ubicacion(lat, lon));
    }

    @Test
    void menorDistanciaEligeElMasCercanoYCambiaEstados() {
        Dron lejos = entrega("LEJOS", 90, 50, 10, 6.30, -75.60);
        Dron cerca = entrega("CERCA", 40, 50, 10, 6.245, -75.582);
        Mision mision = misionEntrega(10, 5);

        ResultadoAsignacion r = motor.asignar(mision, List.of(lejos, cerca), RESERVA, null);

        assertTrue(r.fueExitoso());
        assertEquals("CERCA", r.getAsignacion().getDron().getId());
        assertEquals(MenorDistancia.NOMBRE, r.getAsignacion().getEstrategia());
        assertEquals(EstadoDron.ASIGNADO, cerca.getEstado());
        assertEquals(EstadoDron.DISPONIBLE, lejos.getEstado());
        assertEquals(EstadoMision.ASIGNADA, mision.getEstado());
    }

    @Test
    void mayorBateriaEligeElDeMasBateria() {
        Dron lejos = entrega("LEJOS", 90, 50, 10, 6.30, -75.60);
        Dron cerca = entrega("CERCA", 40, 50, 10, 6.245, -75.582);

        ResultadoAsignacion r = motor.asignar(misionEntrega(10, 5), List.of(lejos, cerca), RESERVA, "MAYOR_BATERIA");

        assertEquals("LEJOS", r.getAsignacion().getDron().getId());
    }

    @Test
    void empateDeDistanciaSeDesempataPorMayorBateria() {
        Dron a = entrega("A", 50, 50, 10, 6.25, -75.58);
        Dron b = entrega("B", 80, 50, 10, 6.25, -75.58);

        ResultadoAsignacion r = motor.asignar(misionEntrega(10, 5), List.of(a, b), RESERVA, "MENOR_DISTANCIA");

        assertEquals("B", r.getAsignacion().getDron().getId());
    }

    @Test
    void laEstrategiaNuncaEligeUnDronNoElegible() {
        Dron cercaSinAutonomia = entrega("CERCA", 100, 12, 10, 6.2443, -75.5813);
        Dron lejosElegible = entrega("LEJOS", 30, 100, 10, 6.40, -75.70);

        ResultadoAsignacion r = motor.asignar(misionEntrega(10, 5), List.of(cercaSinAutonomia, lejosElegible), RESERVA, null);

        assertEquals("LEJOS", r.getAsignacion().getDron().getId());
    }

    @Test
    void sinCandidatosExplicaLosMotivosYDejaLaMisionPendiente() {
        Dron pocaCapacidad = entrega("D1", 80, 100, 2, 6.25, -75.58);
        Dron pocaAutonomia = entrega("D2", 80, 14, 10, 6.25, -75.58);
        Mision mision = misionEntrega(10, 5);

        ResultadoAsignacion r = motor.asignar(mision, List.of(pocaCapacidad, pocaAutonomia), RESERVA, null);

        assertFalse(r.fueExitoso());
        assertEquals(List.of(MotivoRechazo.CAPACIDAD_INSUFICIENTE, MotivoRechazo.AUTONOMIA_INSUFICIENTE), r.getMotivos());
        assertEquals(EstadoMision.PENDIENTE, mision.getEstado());
    }

    @Test
    void sinDronesDelTipoInformaTipoIncompatible() {
        Dron foto = new DronFotografia("F1", 80, 100, 64, EstadoDron.DISPONIBLE, DESTINO);

        ResultadoAsignacion r = motor.asignar(misionEntrega(10, 5), List.of(foto), RESERVA, null);

        assertEquals(List.of(MotivoRechazo.TIPO_INCOMPATIBLE), r.getMotivos());
    }

    @Test
    void dronAsignadoNoEsCandidato() {
        Dron ocupado = new DronEntrega("D1", 80, 100, 10, EstadoDron.ASIGNADO, DESTINO);

        ResultadoAsignacion r = motor.asignar(misionEntrega(10, 5), List.of(ocupado), RESERVA, null);

        assertEquals(List.of(MotivoRechazo.NO_DISPONIBLE), r.getMotivos());
    }

    @Test
    void estrategiaDesconocidaSeRechaza() {
        Mision mision = misionEntrega(10, 5);
        List<Dron> flota = List.of(entrega("D1", 80, 100, 10, 6.25, -75.58));
        assertThrows(IllegalArgumentException.class, () -> motor.asignar(mision, flota, RESERVA, "AL_AZAR"));
        assertEquals(EstadoMision.PENDIENTE, mision.getEstado());
    }

    @Test
    void misionYaAsignadaNoSeReasigna() {
        Mision mision = misionEntrega(10, 5);
        mision.marcarAsignada();
        assertThrows(IllegalStateException.class,
                () -> motor.asignar(mision, List.of(entrega("D1", 80, 100, 10, 6.25, -75.58)), RESERVA, null));
    }

    @Test
    void laReservaOperativaSeSumaALaDistancia() {
        assertTrue(new ReservaOperativa(5).cubre(15, 10));
        assertFalse(new ReservaOperativa(5).cubre(14.9, 10));
    }
}
