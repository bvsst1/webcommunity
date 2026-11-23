package com.gamercomunity.controllers;

import com.gamercomunity.service.EventoService;
import com.gamercomunity.service.InteraccionEventoService;
import java.io.IOException;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/eventos/{id}")
public class ApiEventoController {
    private final EventoService eventos;
    private final InteraccionEventoService interacciones;

    public ApiEventoController(EventoService eventos, InteraccionEventoService interacciones) {
        this.eventos = eventos;
        this.interacciones = interacciones;
    }

    @GetMapping
    public EventoResponse detalle(@PathVariable Long id) {
        var evento = eventos.obtenerPorId(id);
        return new EventoResponse(evento.getId(), evento.getNombre(), evento.getDescripcion(),
                evento.isCompartido(), interacciones.promedio(id));
    }

    @PostMapping("/comentarios")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void comentar(@PathVariable Long id, @RequestBody ComentarioRequest solicitud, Principal usuario) {
        interacciones.comentar(id, usuario.getName(), solicitud.texto());
    }

    @PostMapping("/valoracion")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void valorar(@PathVariable Long id, @RequestBody ValoracionRequest solicitud, Principal usuario) {
        interacciones.valorar(id, usuario.getName(), solicitud.puntaje());
    }

    @PostMapping("/compartir")
    public CompartirResponse compartir(@PathVariable Long id) {
        interacciones.compartir(id);
        return new CompartirResponse("/eventos/compartidos/" + id);
    }

    @PostMapping("/multimedia")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void agregarArchivo(@PathVariable Long id, @RequestParam MultipartFile archivo) throws IOException {
        interacciones.agregarArchivo(id, archivo);
    }

    public record EventoResponse(Long id, String nombre, String descripcion, boolean compartido, Double valoracion) {}
    public record ComentarioRequest(String texto) {}
    public record ValoracionRequest(int puntaje) {}
    public record CompartirResponse(String ruta) {}
}
