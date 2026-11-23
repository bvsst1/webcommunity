package com.gamercomunity.service;

import com.gamercomunity.models.*;
import com.gamercomunity.repository.*;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InteraccionEventoService {
    private final EventoService eventos;
    private final UsuarioService usuarios;
    private final ComentarioEventoRepository comentarios;
    private final ValoracionEventoRepository valoraciones;
    private final MultimediaEventoRepository multimedia;

    public InteraccionEventoService(EventoService eventos, UsuarioService usuarios,
            ComentarioEventoRepository comentarios, ValoracionEventoRepository valoraciones,
            MultimediaEventoRepository multimedia) {
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.comentarios = comentarios;
        this.valoraciones = valoraciones;
        this.multimedia = multimedia;
    }

    public List<ComentarioEventoRepository.Detalle> comentarios(Long eventoId) {
        return comentarios.listar(eventoId);
    }

    public List<MultimediaEventoRepository.Detalle> archivos(Long eventoId) {
        return multimedia.listar(eventoId);
    }

    public Double promedio(Long eventoId) {
        return valoraciones.promedio(eventoId);
    }

    public void comentar(Long eventoId, String username, String texto) {
        String comentario = texto == null ? "" : texto.strip();
        if (comentario.isEmpty() || comentario.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El comentario debe tener entre 1 y 1000 caracteres");
        }
        comentarios.save(new ComentarioEvento(eventos.obtenerPorId(eventoId),
                usuarios.obtenerPorUsername(username), comentario));
    }

    @Transactional
    public void valorar(Long eventoId, String username, int puntaje) {
        if (puntaje < 1 || puntaje > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La valoración debe ser de 1 a 5");
        }
        Evento evento = eventos.obtenerParaActualizar(eventoId);
        Usuario usuario = usuarios.obtenerPorUsername(username);
        ValoracionEvento valoracion = valoraciones.findByEventoIdAndAutorId(eventoId, usuario.getId())
                .orElseGet(() -> new ValoracionEvento(evento, usuario));
        valoracion.setPuntaje(puntaje);
        valoraciones.save(valoracion);
    }

    @Transactional
    public void compartir(Long eventoId) {
        eventos.obtenerParaActualizar(eventoId).setCompartido(true);
    }

    public void agregarArchivo(Long eventoId, MultipartFile archivo) throws IOException {
        Evento evento = eventos.obtenerPorId(eventoId);
        byte[] contenido = archivo.getBytes();
        String tipo = identificarTipo(contenido);
        multimedia.save(new MultimediaEvento(evento, tipo, contenido));
    }

    public MultimediaEvento obtenerArchivo(Long eventoId, Long archivoId) {
        return multimedia.findByIdAndEventoId(archivoId, eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Archivo no encontrado"));
    }

    private String identificarTipo(byte[] bytes) {
        if (bytes.length >= 8 && coincide(bytes, 0, 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a)) {
            return "image/png";
        }
        if (bytes.length >= 4 && coincide(bytes, 0, 0xff, 0xd8, 0xff)) {
            return "image/jpeg";
        }
        if (bytes.length >= 16 && coincide(bytes, 4, 'f', 't', 'y', 'p')) {
            return "video/mp4";
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona una imagen PNG o JPEG, o un video MP4");
    }

    private boolean coincide(byte[] bytes, int offset, int... firma) {
        for (int i = 0; i < firma.length; i++) {
            if (Byte.toUnsignedInt(bytes[offset + i]) != firma[i]) {
                return false;
            }
        }
        return true;
    }
}
