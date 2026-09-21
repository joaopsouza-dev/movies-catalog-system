package br.com.catalogofilmes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Filme do catálogo (campos da tabela 3.2 da História de Usuário).
 */
@Entity
@Table(name = "filmes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"titulo", "ano_lancamento"}))
public class Filme {

    public static final int TITULO_MAX = 100;
    public static final int GENERO_MAX = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = TITULO_MAX)
    private String titulo;

    @Column(nullable = false, length = GENERO_MAX)
    private String genero;

    @Column(name = "ano_lancamento", nullable = false)
    private Integer anoLancamento;

    /** Duração em minutos. */
    @Column(nullable = false)
    private Integer duracao;

    public Filme() {
    }

    public Filme(String titulo, String genero, Integer anoLancamento, Integer duracao) {
        this.titulo = titulo;
        this.genero = genero;
        this.anoLancamento = anoLancamento;
        this.duracao = duracao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public Integer getAnoLancamento() {
        return anoLancamento;
    }

    public void setAnoLancamento(Integer anoLancamento) {
        this.anoLancamento = anoLancamento;
    }

    public Integer getDuracao() {
        return duracao;
    }

    public void setDuracao(Integer duracao) {
        this.duracao = duracao;
    }

    @Override
    public String toString() {
        return "Filme{id=" + id + ", titulo='" + titulo + "', genero='" + genero
                + "', anoLancamento=" + anoLancamento + ", duracao=" + duracao + "}";
    }
}
