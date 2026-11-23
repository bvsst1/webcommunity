package com.gamercomunity.models;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

@Entity
@Table(name = "comentarios_evento")
public class ComentarioEvento {
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

    @Column(nullable = false, length = 1000)
    private String texto;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected ComentarioEvento() {}

    public ComentarioEvento(Evento evento, Usuario autor, String texto) {
        this.evento = evento;
        this.autor = autor;
        this.texto = texto;
        this.fecha = LocalDateTime.now();
    }
}
