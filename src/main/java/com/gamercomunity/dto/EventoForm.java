package com.gamercomunity.dto;

import com.gamercomunity.models.Evento;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public record EventoForm(Long id, String nombre, String descripcion, String direccion,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHora,
        String organizadores, String expositoresPrincipales, String categoria,
        Boolean tieneEstacionamiento, Boolean tieneLocomocion, Boolean tieneComidas, Boolean tieneAlojamiento) {

    public Evento toEvento() {
        if (nombre == null || nombre.isBlank() || nombre.length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa un nombre de evento de hasta 200 caracteres");
        }
        Evento evento = new Evento();
        evento.setId(id);
        evento.setNombre(nombre.strip());
        evento.setDescripcion(descripcion);
        evento.setDireccion(direccion);
        evento.setFechaHora(fechaHora);
        evento.setOrganizadores(organizadores);
        evento.setExpositoresPrincipales(expositoresPrincipales);
        evento.setCategoria(categoria);
        evento.setTieneEstacionamiento(Boolean.TRUE.equals(tieneEstacionamiento));
        evento.setTieneLocomocion(Boolean.TRUE.equals(tieneLocomocion));
        evento.setTieneComidas(Boolean.TRUE.equals(tieneComidas));
        evento.setTieneAlojamiento(Boolean.TRUE.equals(tieneAlojamiento));
        return evento;
    }
}
