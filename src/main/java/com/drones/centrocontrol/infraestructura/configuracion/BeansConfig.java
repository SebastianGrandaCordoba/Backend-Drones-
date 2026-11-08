package com.drones.centrocontrol.infraestructura.configuracion;

import com.drones.centrocontrol.aplicacion.servicio.AsignacionService;
import com.drones.centrocontrol.aplicacion.servicio.ConfiguracionService;
import com.drones.centrocontrol.aplicacion.servicio.GestionFlotaService;
import com.drones.centrocontrol.aplicacion.servicio.GestionMisionesService;
import com.drones.centrocontrol.dominio.estrategia.MayorBateria;
import com.drones.centrocontrol.dominio.estrategia.MenorDistancia;
import com.drones.centrocontrol.dominio.servicio.MotorAsignacion;
import com.drones.centrocontrol.infraestructura.salida.mongo.AsignacionMongoAdapter;
import com.drones.centrocontrol.infraestructura.salida.mongo.ConfiguracionMongoAdapter;
import com.drones.centrocontrol.infraestructura.salida.mongo.DronMongoAdapter;
import com.drones.centrocontrol.infraestructura.salida.mongo.MisionMongoAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;
import java.util.List;

/**
 * Ensambla la arquitectura hexagonal: crea los adaptadores de salida, el motor de dominio
 * y los servicios de aplicación. El dominio y la aplicación no dependen de Spring;
 * solo esta clase conoce qué implementación concreta se conecta a cada puerto.
 */
@Configuration
public class BeansConfig {

    @Bean
    public Clock reloj() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public DronMongoAdapter dronMongoAdapter(MongoTemplate mongo) {
        return new DronMongoAdapter(mongo);
    }

    @Bean
    public MisionMongoAdapter misionMongoAdapter(MongoTemplate mongo) {
        return new MisionMongoAdapter(mongo);
    }

    @Bean
    public AsignacionMongoAdapter asignacionMongoAdapter(MongoTemplate mongo, MisionMongoAdapter misiones,
                                                         DronMongoAdapter drones) {
        return new AsignacionMongoAdapter(mongo, misiones, drones);
    }

    @Bean
    public ConfiguracionMongoAdapter configuracionMongoAdapter(MongoTemplate mongo) {
        return new ConfiguracionMongoAdapter(mongo);
    }

    /** Para agregar una estrategia basta con incluir aquí su implementación. */
    @Bean
    public MotorAsignacion motorAsignacion() {
        return new MotorAsignacion(List.of(new MenorDistancia(), new MayorBateria()));
    }

    @Bean
    public GestionFlotaService gestionFlotaService(DronMongoAdapter drones) {
        return new GestionFlotaService(drones);
    }

    @Bean
    public ConfiguracionService configuracionService(ConfiguracionMongoAdapter configuracion) {
        return new ConfiguracionService(configuracion);
    }

    @Bean
    public GestionMisionesService gestionMisionesService(MisionMongoAdapter misiones, AsignacionMongoAdapter asignaciones,
                                                         DronMongoAdapter drones, GestionFlotaService flota, Clock reloj) {
        return new GestionMisionesService(misiones, asignaciones, drones, flota, reloj);
    }

    @Bean
    public AsignacionService asignacionService(GestionMisionesService misiones, GestionFlotaService flota,
                                               ConfiguracionService configuracion, MotorAsignacion motor,
                                               DronMongoAdapter drones, MisionMongoAdapter misionesRepo,
                                               AsignacionMongoAdapter asignaciones) {
        return new AsignacionService(misiones, flota, configuracion, motor, drones, misionesRepo, asignaciones);
    }

    /** Permite que el frontend (otro origen) consuma la API REST. */
    @Bean
    public WebMvcConfigurer cors(@Value("${app.cors.origenes}") String[] origenes) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registro) {
                registro.addMapping("/api/**").allowedOrigins(origenes).allowedMethods("GET", "POST", "PUT");
            }
        };
    }
}
