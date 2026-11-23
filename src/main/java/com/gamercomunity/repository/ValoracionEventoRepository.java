package com.gamercomunity.repository;

import com.gamercomunity.models.ValoracionEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface ValoracionEventoRepository extends JpaRepository<ValoracionEvento, Long> {
    Optional<ValoracionEvento> findByEventoIdAndAutorId(Long eventoId, Long autorId);

    @Query("select avg(v.puntaje) from ValoracionEvento v where v.evento.id = :eventoId")
    Double promedio(Long eventoId);
}
