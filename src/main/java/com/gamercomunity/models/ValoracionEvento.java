package com.gamercomunity.models;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "valoraciones_evento", uniqueConstraints = @UniqueConstraint(columnNames = {"evento_id", "usuario_id"}))
public class ValoracionEvento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Usuario autor;

    @Column(nullable = false)
    private int puntaje;

    protected ValoracionEvento() {}

    public ValoracionEvento(Evento evento, Usuario autor) {
        this.evento = evento;
        this.autor = autor;
    }

    public void setPuntaje(int puntaje) { this.puntaje = puntaje; }
}
