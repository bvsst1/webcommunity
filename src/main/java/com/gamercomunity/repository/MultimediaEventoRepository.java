package com.gamercomunity.repository;

import com.gamercomunity.models.MultimediaEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface MultimediaEventoRepository extends JpaRepository<MultimediaEvento, Long> {
    Optional<MultimediaEvento> findByIdAndEventoId(Long id, Long eventoId);

    @Query("select m.id as id, m.tipo as tipo from MultimediaEvento m where m.evento.id = :eventoId order by m.id")
    List<Detalle> listar(Long eventoId);

    interface Detalle {
        Long getId();
        String getTipo();
    }
}
