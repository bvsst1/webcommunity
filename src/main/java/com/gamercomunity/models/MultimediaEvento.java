package com.gamercomunity.models;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "multimedia_evento")
public class MultimediaEvento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Evento evento;

    @Column(nullable = false, length = 25)
    private String tipo;

    @Lob
    @Column(nullable = false, columnDefinition = "MEDIUMBLOB")
    private byte[] contenido;

    protected MultimediaEvento() {}

    public MultimediaEvento(Evento evento, String tipo, byte[] contenido) {
        this.evento = evento;
        this.tipo = tipo;
        this.contenido = contenido;
    }

    public String getTipo() { return tipo; }
    public byte[] getContenido() { return contenido; }
}
