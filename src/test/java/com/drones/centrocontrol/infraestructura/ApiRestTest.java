package com.drones.centrocontrol.infraestructura;

import com.drones.centrocontrol.aplicacion.puerto.entrada.IAsignacion;
import com.drones.centrocontrol.aplicacion.puerto.entrada.IConfiguracionOperativa;
import com.drones.centrocontrol.aplicacion.puerto.entrada.IConsultaFlota;
import com.drones.centrocontrol.aplicacion.puerto.entrada.IConsultaMisiones;
import com.drones.centrocontrol.aplicacion.puerto.entrada.IGestionFlota;
import com.drones.centrocontrol.aplicacion.puerto.entrada.IGestionMisiones;
import com.drones.centrocontrol.dominio.modelo.Asignacion;
import com.drones.centrocontrol.dominio.modelo.DronEntrega;
import com.drones.centrocontrol.dominio.modelo.EstadoDron;
import com.drones.centrocontrol.dominio.modelo.EstadoMision;
import com.drones.centrocontrol.dominio.modelo.Mision;
import com.drones.centrocontrol.dominio.modelo.MotivoRechazo;
import com.drones.centrocontrol.dominio.modelo.Prioridad;
import com.drones.centrocontrol.dominio.modelo.ResultadoAsignacion;
import com.drones.centrocontrol.dominio.modelo.TipoMision;
import com.drones.centrocontrol.dominio.modelo.Ubicacion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas del adaptador de entrada REST: verifican el contrato HTTP (códigos de estado,
 * validación y traducción de errores) sin MongoDB, simulando los puertos de entrada.
 */
@WebMvcTest
class ApiRestTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private IGestionFlota gestionFlota;
    @MockBean
    private IConsultaFlota consultaFlota;
    @MockBean
    private IGestionMisiones gestionMisiones;
    @MockBean
    private IConsultaMisiones consultaMisiones;
    @MockBean
    private IAsignacion asignacion;
    @MockBean
    private IConfiguracionOperativa configuracion;

    private static final Ubicacion BOGOTA = new Ubicacion(4.6, -74.08);

    @Test
    void registrarDronDevuelve201ConLaUnidadDeCapacidad() throws Exception {
        when(gestionFlota.registrar(eq("E-1"), eq(TipoMision.ENTREGA), anyDouble(), anyDouble(), anyDouble(),
                eq(EstadoDron.DISPONIBLE), any()))
                .thenReturn(new DronEntrega("E-1", 80, 40, 5, EstadoDron.DISPONIBLE, BOGOTA));

        mvc.perform(post("/api/drones").contentType(MediaType.APPLICATION_JSON).content("""
                        {"id":"E-1","tipo":"ENTREGA","bateria":80,"autonomiaKm":40,"capacidad":5,
                         "estado":"DISPONIBLE","ubicacion":{"latitud":4.6,"longitud":-74.08}}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("E-1"))
                .andExpect(jsonPath("$.unidadCapacidad").value("kg"));
    }

    @Test
    void camposFaltantesDevuelven400ConElDetalle() throws Exception {
        mvc.perform(post("/api/drones").contentType(MediaType.APPLICATION_JSON).content("{\"id\":\"E-1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles").isNotEmpty());
    }

    @Test
    void valorDeEnumeracionInvalidoDevuelve400() throws Exception {
        mvc.perform(get("/api/drones").param("tipo", "SUBMARINO"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void recursoInexistenteDevuelve404() throws Exception {
        when(consultaFlota.buscarPorId("X")).thenThrow(new NoSuchElementException("No se encontró el dron X"));

        mvc.perform(get("/api/drones/X"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No se encontró el dron X"));
    }

    @Test
    void transicionNoPermitidaDevuelve409() throws Exception {
        when(gestionMisiones.completar("M-1")).thenThrow(new IllegalStateException("No se puede completar"));

        mvc.perform(post("/api/misiones/M-1/completar"))
                .andExpect(status().isConflict());
    }

    @Test
    void asignacionExitosaDevuelve201ConJustificacion() throws Exception {
        Mision mision = new Mision("M-1", TipoMision.ENTREGA, Prioridad.ALTA, 10, LocalDateTime.now().plusDays(1),
                BOGOTA, 3, EstadoMision.ASIGNADA);
        DronEntrega dron = new DronEntrega("E-1", 80, 40, 5, EstadoDron.ASIGNADO, BOGOTA);
        Asignacion a = new Asignacion("A-1", mision, dron, "MENOR_DISTANCIA", LocalDateTime.now(), 0.5, 80, true);
        when(asignacion.asignar("M-1", null)).thenReturn(new ResultadoAsignacion(a, List.of()));

        mvc.perform(post("/api/asignaciones").contentType(MediaType.APPLICATION_JSON).content("{\"misionId\":\"M-1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.asignado").value(true))
                .andExpect(jsonPath("$.asignacion.dronId").value("E-1"))
                .andExpect(jsonPath("$.asignacion.justificacion").isNotEmpty());
    }

    @Test
    void sinCandidatoDevuelve200ConMotivosPorqueNoEsUnError() throws Exception {
        when(asignacion.asignar("M-2", null))
                .thenReturn(new ResultadoAsignacion(null, List.of(MotivoRechazo.AUTONOMIA_INSUFICIENTE)));

        mvc.perform(post("/api/asignaciones").contentType(MediaType.APPLICATION_JSON).content("{\"misionId\":\"M-2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.asignado").value(false))
                .andExpect(jsonPath("$.motivos[0].codigo").value("AUTONOMIA_INSUFICIENTE"));
    }

    @Test
    void reservaEnUnidadDistintaDeKmDevuelve400() throws Exception {
        when(configuracion.configurarReserva(5.0, "mi"))
                .thenThrow(new IllegalArgumentException("La reserva debe expresarse en km"));

        mvc.perform(put("/api/configuracion/reserva").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":5,\"unidad\":\"mi\"}"))
                .andExpect(status().isBadRequest());
    }
}
