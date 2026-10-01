package br.com.catalogofilmes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.catalogofilmes.exception.DadosInvalidosException;
import br.com.catalogofilmes.exception.FilmeDuplicadoException;
import br.com.catalogofilmes.exception.FilmeNaoEncontradoException;
import br.com.catalogofilmes.integracao.OmdbClient;
import br.com.catalogofilmes.model.Filme;
import br.com.catalogofilmes.repository.FilmeRepository;

/** Testes unitários dos quatro casos de teste (CT01 a CT04). */
@ExtendWith(MockitoExtension.class)
class FilmeServiceTest {

    @Mock
    private FilmeRepository repository;

    @Mock
    private OmdbClient omdbClient;

    private FilmeService service;

    @BeforeEach
    void preparar() {
        Clock relogio = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneOffset.UTC);
        service = new FilmeService(repository, omdbClient, relogio);
    }

    // CT01 - cadastrar filme válido.
    @Test
    void cadastrarFilmeValido() {
        when(repository.existsByTituloIgnoreCaseAndAnoLancamento("Matrix", 1999)).thenReturn(false);
        when(repository.save(any(Filme.class))).thenAnswer(inv -> {
            Filme filme = inv.getArgument(0);
            filme.setId(1L);
            return filme;
        });

        Filme salvo = service.cadastrar(new Filme("Matrix", "Ficcao", 1999, 136));

        assertThat(salvo.getId()).isEqualTo(1L);
        assertThat(salvo.getTitulo()).isEqualTo("Matrix");
        assertThat(salvo.getGenero()).isEqualTo("Ficcao");
        assertThat(salvo.getAnoLancamento()).isEqualTo(1999);
        assertThat(salvo.getDuracao()).isEqualTo(136);
        verify(repository).save(any(Filme.class));
    }

    // CT02 - rejeitar cadastro sem título e sem gênero.
    @Test
    void rejeitarFilmeSemCampoObrigatorio() {
        DadosInvalidosException semTitulo = assertThrows(DadosInvalidosException.class,
                () -> service.cadastrar(new Filme(" ", "Ficcao", 1999, 136)));
        DadosInvalidosException semGenero = assertThrows(DadosInvalidosException.class,
                () -> service.cadastrar(new Filme("Matrix", "", 1999, 136)));

        assertThat(semTitulo.getErros()).contains("O título é obrigatório.");
        assertThat(semGenero.getErros()).contains("O gênero é obrigatório.");
        verify(repository, never()).save(any());
    }

    // CT03 - impedir cadastro de filme com título e ano já existentes.
    @Test
    void impedirCadastroDeFilmeDuplicado() {
        when(repository.existsByTituloIgnoreCaseAndAnoLancamento("Matrix", 1999)).thenReturn(true);

        assertThrows(FilmeDuplicadoException.class,
                () -> service.cadastrar(new Filme("Matrix", "Ficcao", 1999, 136)));

        verify(repository, never()).save(any());
    }

    // CT04 - retornar erro ao buscar ID que não está cadastrado.
    @Test
    void retornarErroAoBuscarFilmeInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        FilmeNaoEncontradoException erro = assertThrows(FilmeNaoEncontradoException.class,
                () -> service.buscarPorId(99L));

        assertThat(erro.getMessage()).contains("99").contains("não encontrado");
    }
}
