package com.drones.centrocontrol.infraestructura.salida.mongo;

import com.drones.centrocontrol.aplicacion.puerto.salida.MisionRepositorio;
import com.drones.centrocontrol.dominio.modelo.EstadoMision;
import com.drones.centrocontrol.dominio.modelo.Mision;
import com.drones.centrocontrol.dominio.modelo.Prioridad;
import com.drones.centrocontrol.dominio.modelo.TipoMision;
import com.drones.centrocontrol.dominio.modelo.Ubicacion;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Adaptador de salida: implementa {@link MisionRepositorio} sobre la colección «misiones». */
public class MisionMongoAdapter implements MisionRepositorio {

    private final MongoTemplate mongo;

    public MisionMongoAdapter(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public Mision guardar(Mision mision) {
        mongo.save(MisionDocumento.desde(mision));
        return mision;
    }

    @Override
    public Optional<Mision> buscarPorId(String id) {
        return Optional.ofNullable(mongo.findById(id, MisionDocumento.class)).map(MisionDocumento::aDominio);
    }

    @Override
    public List<Mision> buscarPorEstado(EstadoMision estado) {
        Query consulta = Query.query(Criteria.where("estado").is(estado.name()));
        return mongo.find(consulta, MisionDocumento.class).stream().map(MisionDocumento::aDominio).toList();
    }

    @Override
    public boolean marcarAsignadaSiPendiente(String id) {
        Query soloSiPendiente = Query.query(Criteria.where("_id").is(id).and("estado").is(EstadoMision.PENDIENTE.name()));
        Update asignar = Update.update("estado", EstadoMision.ASIGNADA.name());
        return mongo.updateFirst(soloSiPendiente, asignar, MisionDocumento.class).getModifiedCount() == 1;
    }

    @Document(collection = "misiones")
    static class MisionDocumento {
        @Id
        String id;
        String tipo;
        String prioridad;
        double distanciaKm;
        LocalDateTime limiteTiempo;
        double destinoLatitud;
        double destinoLongitud;
        double capacidadRequerida;
        String estado;

        static MisionDocumento desde(Mision m) {
            MisionDocumento d = new MisionDocumento();
            d.id = m.getId();
            d.tipo = m.getTipo().name();
            d.prioridad = m.getPrioridad().name();
            d.distanciaKm = m.getDistanciaKm();
            d.limiteTiempo = m.getLimiteTiempo();
            d.destinoLatitud = m.getDestino().getLatitud();
            d.destinoLongitud = m.getDestino().getLongitud();
            d.capacidadRequerida = m.getCapacidadRequerida();
            d.estado = m.getEstado().name();
            return d;
        }

        Mision aDominio() {
            return new Mision(id, TipoMision.valueOf(tipo), Prioridad.valueOf(prioridad), distanciaKm, limiteTiempo,
                    new Ubicacion(destinoLatitud, destinoLongitud), capacidadRequerida, EstadoMision.valueOf(estado));
        }
    }
}
