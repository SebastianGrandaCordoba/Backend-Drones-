package com.drones.centrocontrol.infraestructura.entrada.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Traduce las excepciones del dominio y de la aplicación a respuestas HTTP:
 * datos inválidos → 400, recurso inexistente → 404, regla de negocio o transición no permitida → 409.
 */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorRespuesta> datosInvalidos(IllegalArgumentException e) {
        return respuesta(HttpStatus.BAD_REQUEST, e.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> camposInvalidos(MethodArgumentNotValidException e) {
        List<String> campos = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage()).toList();
        return respuesta(HttpStatus.BAD_REQUEST, "Hay campos obligatorios faltantes o inválidos", campos);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorRespuesta> formatoInvalido(Exception e) {
        return respuesta(HttpStatus.BAD_REQUEST, "El formato o un valor de la solicitud no es válido", List.of());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorRespuesta> noEncontrado(NoSuchElementException e) {
        return respuesta(HttpStatus.NOT_FOUND, e.getMessage(), List.of());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorRespuesta> reglaDeNegocio(IllegalStateException e) {
        return respuesta(HttpStatus.CONFLICT, e.getMessage(), List.of());
    }

    private ResponseEntity<ErrorRespuesta> respuesta(HttpStatus estado, String mensaje, List<String> detalles) {
        return ResponseEntity.status(estado)
                .body(new ErrorRespuesta(estado.value(), mensaje, detalles, LocalDateTime.now()));
    }

    public record ErrorRespuesta(int estado, String mensaje, List<String> detalles, LocalDateTime fecha) {
    }
}
