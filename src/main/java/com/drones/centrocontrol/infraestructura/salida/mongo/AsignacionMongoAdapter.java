package com.drones.centrocontrol.infraestructura.salida.mongo;

import com.drones.centrocontrol.aplicacion.puerto.salida.AsignacionRepositorio;
import com.drones.centrocontrol.aplicacion.puerto.salida.DronRepositorio;
import com.drones.centrocontrol.aplicacion.puerto.salida.MisionRepositorio;
import com.drones.centrocontrol.dominio.modelo.Asignacion;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Adaptador de salida: implementa {@link AsignacionRepositorio} sobre la colección «asignaciones».
 * Guarda las referencias a misión y dron por identificador y reconstruye las asociaciones al leer.
 */
public class AsignacionMongoAdapter implements AsignacionRepositorio {

    private final MongoTemplate mongo;
    private final MisionRepositorio misiones;
    private final DronRepositorio drones;

    public AsignacionMongoAdapter(MongoTemplate mongo, MisionRepositorio misiones, DronRepositorio drones) {
        this.mongo = mongo;
        this.misiones = misiones;
        this.drones = drones;
    }

    @Override
    public Asignacion guardar(Asignacion asignacion) {
        mongo.save(AsignacionDocumento.desde(asignacion));
        return asignacion;
    }

    @Override
    public Optional<Asignacion> buscarActivaPorMision(String misionId) {
        Query consulta = Query.query(Criteria.where("misionId").is(misionId).and("activa").is(true));
        return Optional.ofNullable(mongo.findOne(consulta, AsignacionDocumento.class)).map(this::aDominio);
    }

    @Override
    public List<Asignacion> buscar(String dronId, String misionId) {
        Criteria criterio = new Criteria();
        if (dronId != null && !dronId.isBlank()) {
            criterio = criterio.and("dronId").is(dronId);
        }
        if (misionId != null && !misionId.isBlank()) {
            criterio = criterio.and("misionId").is(misionId);
        }
        Query consulta = Query.query(criterio).with(Sort.by(Sort.Direction.DESC, "fecha"));
        return mongo.find(consulta, AsignacionDocumento.class).stream().map(this::aDominio).toList();
    }

    private Asignacion aDominio(AsignacionDocumento d) {
        return new Asignacion(d.id,
                misiones.buscarPorId(d.misionId).orElseThrow(() -> new NoSuchElementException("Misión " + d.misionId)),
                drones.buscarPorId(d.dronId).orElseThrow(() -> new NoSuchElementException("Dron " + d.dronId)),
                d.estrategia, d.fecha, d.distanciaAlDestinoKm, d.bateriaAlAsignar, d.activa);
    }

    @Document(collection = "asignaciones")
    static class AsignacionDocumento {
        @Id
        String id;
        String misionId;
        String dronId;
        String estrategia;
        LocalDateTime fecha;
        double distanciaAlDestinoKm;
        double bateriaAlAsignar;
        boolean activa;

        static AsignacionDocumento desde(Asignacion a) {
            AsignacionDocumento d = new AsignacionDocumento();
            d.id = a.getId();
            d.misionId = a.getMision().getId();
            d.dronId = a.getDron().getId();
            d.estrategia = a.getEstrategia();
            d.fecha = a.getFecha();
            d.distanciaAlDestinoKm = a.getDistanciaAlDestinoKm();
            d.bateriaAlAsignar = a.getBateriaAlAsignar();
            d.activa = a.isActiva();
            return d;
        }
    }
}
