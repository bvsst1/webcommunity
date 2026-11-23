package com.gamercomunity.service;

import com.gamercomunity.models.Evento;
import com.gamercomunity.repository.EventoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;

    public EventoService(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    public List<Evento> listarProximos() {
        return eventoRepository.findAllByOrderByFechaHoraAsc();
    }

    public List<Evento> buscar(String categoria, LocalDateTime fecha, String ubicacion) {
        return eventoRepository.buscarEventos(categoria, fecha, ubicacion);
    }

    public Evento obtenerPorId(Long id) {
        return eventoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
    }

    public Evento obtenerParaActualizar(Long id) {
        return eventoRepository.buscarParaActualizar(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
    }

    public List<Evento> listarCompartidos() {
        return eventoRepository.findByCompartidoTrueOrderByFechaHoraAsc();
    }

    @Transactional
    public Evento guardar(Evento evento) {
        if (evento.getId() != null) {
            evento.setCompartido(obtenerParaActualizar(evento.getId()).isCompartido());
        } else {
            evento.setCompartido(false);
        }
        return eventoRepository.save(evento);
    }

    public void eliminar(Long id) {
        eventoRepository.deleteById(id);
    }
}
