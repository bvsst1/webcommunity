package com.gamercomunity.controllers;

import com.gamercomunity.models.Evento;
import com.gamercomunity.service.EventoService;
import com.gamercomunity.service.InteraccionEventoService;
import java.io.IOException;
import java.security.Principal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/eventos")
public class EventoController {

    private final EventoService eventoService;
    private final InteraccionEventoService interacciones;

    public EventoController(EventoService eventoService, InteraccionEventoService interacciones) {
        this.eventoService = eventoService;
        this.interacciones = interacciones;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false, defaultValue = "") String categoria,
            @RequestParam(required = false, defaultValue = "") String ubicacion,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime fecha,
            Model model) {

        List<Evento> eventos = eventoService.buscar(categoria, fecha, ubicacion);
        
        model.addAttribute("eventos", eventos);
        model.addAttribute("categoria", categoria);
        model.addAttribute("fecha", fecha);
        model.addAttribute("ubicacion", ubicacion);
        
        return "eventos/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("evento", eventoService.obtenerPorId(id));
        model.addAttribute("comentarios", interacciones.comentarios(id));
        model.addAttribute("archivos", interacciones.archivos(id));
        model.addAttribute("promedio", interacciones.promedio(id));
        return "eventos/detalle";
    }

    @GetMapping("/compartidos")
    public String compartidos(Model model) {
        model.addAttribute("eventos", eventoService.listarCompartidos());
        return "eventos/compartidos";
    }

    @GetMapping("/compartidos/{id}")
    public String detalleCompartido(@PathVariable Long id, Model model) {
        Evento evento = eventoService.obtenerPorId(id);
        if (!evento.isCompartido()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        model.addAttribute("evento", evento);
        model.addAttribute("vistaCompartida", true);
        return "eventos/detalle";
    }

    @PostMapping("/{id}/comentarios")
    public String comentar(@PathVariable Long id, @RequestParam String texto, Principal usuario) {
        interacciones.comentar(id, usuario.getName(), texto);
        return "redirect:/eventos/" + id;
    }

    @PostMapping("/{id}/valoracion")
    public String valorar(@PathVariable Long id, @RequestParam int puntaje, Principal usuario) {
        interacciones.valorar(id, usuario.getName(), puntaje);
        return "redirect:/eventos/" + id;
    }

    @PostMapping("/{id}/compartir")
    public String compartir(@PathVariable Long id) {
        interacciones.compartir(id);
        return "redirect:/eventos/" + id;
    }

    @PostMapping("/{id}/multimedia")
    public String guardarArchivo(@PathVariable Long id, @RequestParam MultipartFile archivo) throws IOException {
        interacciones.agregarArchivo(id, archivo);
        return "redirect:/eventos/" + id;
    }

    @GetMapping("/{id}/multimedia/{archivoId}")
    public ResponseEntity<byte[]> obtenerArchivo(@PathVariable Long id, @PathVariable Long archivoId) {
        var archivo = interacciones.obtenerArchivo(id, archivoId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.getTipo()))
                .body(archivo.getContenido());
    }
}
