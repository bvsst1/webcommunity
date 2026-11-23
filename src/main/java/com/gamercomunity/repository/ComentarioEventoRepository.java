package com.gamercomunity.repository;

import com.gamercomunity.models.ComentarioEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface ComentarioEventoRepository extends JpaRepository<ComentarioEvento, Long> {
    @Query("""
            select c.autor.username as autor, c.texto as texto, c.fecha as fecha
            from ComentarioEvento c where c.evento.id = :eventoId order by c.id desc
            """)
    List<Detalle> listar(Long eventoId);

    interface Detalle {
        String getAutor();
        String getTexto();
        LocalDateTime getFecha();
    }
}
