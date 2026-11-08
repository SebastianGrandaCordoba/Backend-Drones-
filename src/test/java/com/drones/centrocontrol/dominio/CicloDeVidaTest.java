package com.drones.centrocontrol.dominio;

import com.drones.centrocontrol.dominio.modelo.Dron;
import com.drones.centrocontrol.dominio.modelo.DronAgricultura;
import com.drones.centrocontrol.dominio.modelo.DronCartografia;
import com.drones.centrocontrol.dominio.modelo.DronEntrega;
import com.drones.centrocontrol.dominio.modelo.DronFotografia;
import com.drones.centrocontrol.dominio.modelo.DronVigilancia;
import com.drones.centrocontrol.dominio.modelo.EstadoDron;
import com.drones.centrocontrol.dominio.modelo.EstadoMision;
import com.drones.centrocontrol.dominio.modelo.Mision;
import com.drones.centrocontrol.dominio.modelo.Prioridad;
import com.drones.centrocontrol.dominio.modelo.TipoMision;
import com.drones.centrocontrol.dominio.modelo.Ubicacion;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CicloDeVidaTest {

    private static final Ubicacion U = new Ubicacion(4.6, -74.08);

    private static Mision mision() {
        return new Mision("M", TipoMision.VIGILANCIA, Prioridad.MEDIA, 5, LocalDateTime.now().plusDays(1), U, 1,
                EstadoMision.PENDIENTE);
    }

    @Test
    void misionRecorreSuCicloCompleto() {
        Mision m = mision();
        m.marcarAsignada();
        m.iniciar();
        m.completar();
        assertEquals(EstadoMision.COMPLETADA, m.getEstado());
    }

    @Test
    void misionCompletadaNoPuedeIniciarseOtraVez() {
        Mision m = mision();
        m.marcarAsignada();
        m.iniciar();
        m.completar();
        assertThrows(IllegalStateException.class, m::iniciar);
        assertEquals(EstadoMision.COMPLETADA, m.getEstado());
    }

    @Test
    void misionEnEjecucionNoSeCancela() {
        Mision m = mision();
        m.marcarAsignada();
        m.iniciar();
        assertThrows(IllegalStateException.class, m::cancelar);
    }

    @Test
    void misionConDatosInvalidosSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> new Mision("M", TipoMision.ENTREGA, Prioridad.ALTA, 0,
                LocalDateTime.now().plusDays(1), U, 1, EstadoMision.PENDIENTE));
        assertThrows(IllegalArgumentException.class, () -> new Mision("M", TipoMision.ENTREGA, Prioridad.ALTA, 3,
                LocalDateTime.now().plusDays(1), U, -1, EstadoMision.PENDIENTE));
    }

    @Test
    void dronConDatosFueraDeRangoSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> new DronEntrega("D", 120, 10, 5, EstadoDron.DISPONIBLE, U));
        assertThrows(IllegalArgumentException.class, () -> new DronEntrega("D", 50, 0, 5, EstadoDron.DISPONIBLE, U));
        assertThrows(IllegalArgumentException.class, () -> new DronEntrega("D", 50, 10, 0, EstadoDron.DISPONIBLE, U));
    }

    @Test
    void dronConMisionNoVaAMantenimiento() {
        Dron d = new DronEntrega("D", 50, 10, 5, EstadoDron.DISPONIBLE, U);
        d.marcarAsignado();
        assertThrows(IllegalStateException.class, d::enviarAMantenimiento);
    }

    @Test
    void cadaTipoDeDronTieneSuUnidadDeCapacidad() {
        assertEquals("kg", new DronEntrega("1", 50, 10, 5, EstadoDron.DISPONIBLE, U).getUnidadCapacidad());
        assertEquals("GB", new DronFotografia("2", 50, 10, 5, EstadoDron.DISPONIBLE, U).getUnidadCapacidad());
        assertEquals("h", new DronVigilancia("3", 50, 10, 5, EstadoDron.DISPONIBLE, U).getUnidadCapacidad());
        assertEquals("L", new DronAgricultura("4", 50, 10, 5, EstadoDron.DISPONIBLE, U).getUnidadCapacidad());
        assertEquals("ha", new DronCartografia("5", 50, 10, 5, EstadoDron.DISPONIBLE, U).getUnidadCapacidad());
    }

    @Test
    void distanciaEntreUbicacionesEsAproximadamenteCorrecta() {
        double km = new Ubicacion(6.2442, -75.5812).distanciaKmA(new Ubicacion(4.7110, -74.0721));
        assertEquals(240, km, 10); // Medellín - Bogotá ≈ 240 km en línea recta
    }
}
