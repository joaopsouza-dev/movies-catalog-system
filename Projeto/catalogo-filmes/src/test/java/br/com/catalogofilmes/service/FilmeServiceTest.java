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

/**
 * Testes unitários da camada de regras de negócio (CT01 a CT04 e regras complementares).
 * O repositório e o cliente do OMDb são simulados (Mockito), então nenhum banco ou rede é usado.
 */
@ExtendWith(MockitoExtension.class)
class FilmeServiceTest {

    @Mock
    private FilmeRepository repository;

    @Mock
    private OmdbClient omdbClient;

    private FilmeService service;

    @BeforeEach
    void preparar() {
        // Relógio fixo em 2026: o limite superior do ano de lançamento passa a ser 2031.
        Clock relogio = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneOffset.UTC);
        service = new FilmeService(repository, omdbClient, relogio);
    }

    private Filme matrix() {
        return new Filme("Matrix", "Ficcao", 1999, 136);
    }

    // ------------------------------------------------------------ CT01

    /** CT01 - cadastro de filme válido. */
    @Test
    void cadastrarFilmeValido() {
        when(repository.existsByTituloIgnoreCaseAndAnoLancamento("Matrix", 1999)).thenReturn(false);
        when(repository.save(any(Filme.class))).thenAnswer(inv -> {
            Filme f = inv.getArgument(0);
            f.setId(1L);
            return f;
        });

        Filme salvo = service.cadastrar(matrix());

        assertThat(salvo.getId()).isEqualTo(1L);
        assertThat(salvo.getTitulo()).isEqualTo("Matrix");
        assertThat(salvo.getGenero()).isEqualTo("Ficcao");
        assertThat(salvo.getAnoLancamento()).isEqualTo(1999);
        assertThat(salvo.getDuracao()).isEqualTo(136);
        verify(repository).save(any(Filme.class));
    }

    // ------------------------------------------------------------ CT02

    /** CT02 - título vazio ou nulo. */
    @Test
    void naoCadastrarSemTitulo() {
        Filme semTitulo = new Filme("   ", "Ficcao", 1999, 136);

        DadosInvalidosException e = assertThrows(DadosInvalidosException.class,
                () -> service.cadastrar(semTitulo));

        assertThat(e.getErros()).contains("O título é obrigatório.");
        verify(repository, never()).save(any());
    }

    /** CT02 - gênero vazio ou nulo. */
    @Test
    void naoCadastrarSemGenero() {
        Filme semGenero = new Filme("Matrix", null, 1999, 136);

        DadosInvalidosException e = assertThrows(DadosInvalidosException.class,
                () -> service.cadastrar(semGenero));

        assertThat(e.getErros()).contains("O gênero é obrigatório.");
        verify(repository, never()).save(any());
    }

    /** CT02 - vários campos inválidos ao mesmo tempo geram vários erros. */
    @Test
    void naoCadastrarComVariosCamposInvalidos() {
        Filme invalido = new Filme("", "", null, null);

        DadosInvalidosException e = assertThrows(DadosInvalidosException.class,
                () -> service.cadastrar(invalido));

        assertThat(e.getErros()).hasSize(4);
        verify(repository, never()).save(any());
    }

    @Test
    void naoCadastrarComAnoForaDoIntervalo() {
        DadosInvalidosException antes = assertThrows(DadosInvalidosException.class,
                () -> service.cadastrar(new Filme("Antigo", "Drama", 1700, 90)));
        DadosInvalidosException depois = assertThrows(DadosInvalidosException.class,
                () -> service.cadastrar(new Filme("Futuro", "Drama", 2040, 90)));

        assertThat(antes.getErros()).hasSize(1);
        assertThat(depois.getErros()).hasSize(1);
        verify(repository, never()).save(any());
    }

    @Test
    void naoCadastrarComDuracaoInvalida() {
        DadosInvalidosException zero = assertThrows(DadosInvalidosException.class,
                () -> service.cadastrar(new Filme("Curto", "Drama", 2000, 0)));
        DadosInvalidosException enorme = assertThrows(DadosInvalidosException.class,
                () -> service.cadastrar(new Filme("Longo", "Drama", 2000, 10000)));

        assertThat(zero.getErros()).hasSize(1);
        assertThat(enorme.getErros()).hasSize(1);
        verify(repository, never()).save(any());
    }

    @Test
    void naoCadastrarFilmeNulo() {
        assertThrows(DadosInvalidosException.class, () -> service.cadastrar(null));
        verify(repository, never()).save(any());
    }

    // ------------------------------------------------------------ CT03

    /** CT03 - mesmo título e mesmo ano de um filme já existente. */
    @Test
    void naoCadastrarFilmeDuplicado() {
        when(repository.existsByTituloIgnoreCaseAndAnoLancamento("Matrix", 1999)).thenReturn(true);

        assertThrows(FilmeDuplicadoException.class, () -> service.cadastrar(matrix()));

        verify(repository, never()).save(any());
    }

    // ------------------------------------------------------------ busca

    @Test
    void buscarFilmeExistente() {
        Filme filme = matrix();
        filme.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(filme));

        Filme encontrado = service.buscarPorId(1L);

        assertThat(encontrado).isSameAs(filme);
        assertThat(encontrado.getTitulo()).isEqualTo("Matrix");
    }

    /** CT04 - busca por ID que não está cadastrado. */
    @Test
    void buscarFilmeInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        FilmeNaoEncontradoException e = assertThrows(FilmeNaoEncontradoException.class,
                () -> service.buscarPorId(99L));

        assertThat(e.getMessage()).contains("99");
    }

    @Test
    void buscarComIdNuloRetornaDadosInvalidos() {
        assertThrows(DadosInvalidosException.class, () -> service.buscarPorId(null));
    }

    // ------------------------------------------------------------ alterar

    @Test
    void alterarFilmeExistente() {
        Filme existente = matrix();
        existente.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByTituloIgnoreCaseAndAnoLancamentoAndIdNot("Matrix Reloaded", 2003, 1L))
                .thenReturn(false);
        when(repository.save(any(Filme.class))).thenAnswer(inv -> inv.getArgument(0));

        Filme alterado = service.alterar(1L, new Filme("Matrix Reloaded", "Acao", 2003, 138));

        assertThat(alterado.getId()).isEqualTo(1L);
        assertThat(alterado.getTitulo()).isEqualTo("Matrix Reloaded");
        assertThat(alterado.getGenero()).isEqualTo("Acao");
        assertThat(alterado.getAnoLancamento()).isEqualTo(2003);
        assertThat(alterado.getDuracao()).isEqualTo(138);
    }

    @Test
    void naoAlterarFilmeInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(FilmeNaoEncontradoException.class, () -> service.alterar(99L, matrix()));

        verify(repository, never()).save(any());
    }

    @Test
    void naoAlterarParaTituloEAnoDeOutroFilme() {
        Filme existente = matrix();
        existente.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByTituloIgnoreCaseAndAnoLancamentoAndIdNot("Inception", 2010, 1L))
                .thenReturn(true);

        assertThrows(FilmeDuplicadoException.class,
                () -> service.alterar(1L, new Filme("Inception", "Ficcao", 2010, 148)));

        verify(repository, never()).save(any());
    }

    @Test
    void naoAlterarComDadosInvalidos() {
        Filme existente = matrix();
        existente.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        assertThrows(DadosInvalidosException.class,
                () -> service.alterar(1L, new Filme("", "Acao", 2003, 138)));

        verify(repository, never()).save(any());
    }

    // ------------------------------------------------------------ excluir

    @Test
    void excluirFilmeExistente() {
        Filme existente = matrix();
        existente.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        service.excluir(1L);

        verify(repository).delete(existente);
    }

    @Test
    void naoExcluirFilmeInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(FilmeNaoEncontradoException.class, () -> service.excluir(99L));

        verify(repository, never()).delete(any());
    }

    // ------------------------------------------------------------ OMDb

    @Test
    void consultarNoOmdbSemTituloRetornaDadosInvalidos() {
        assertThrows(DadosInvalidosException.class, () -> service.consultarNoOmdb("  ", null));
    }

    @Test
    void consultarNoOmdbNaoSalvaNoBanco() {
        Filme inception = new Filme("Inception", "Action, Adventure, Sci-Fi", 2010, 148);
        when(omdbClient.buscarPorTitulo("Inception", null)).thenReturn(inception);

        Filme resultado = service.consultarNoOmdb("Inception", null);

        assertThat(resultado.getTitulo()).isEqualTo("Inception");
        verify(repository, never()).save(any());
    }

    @Test
    void importarDoOmdbSalvaNoBanco() {
        Filme inception = new Filme("Inception", "Action, Adventure, Sci-Fi", 2010, 148);
        when(omdbClient.buscarPorTitulo("Inception", 2010)).thenReturn(inception);
        when(repository.existsByTituloIgnoreCaseAndAnoLancamento("Inception", 2010)).thenReturn(false);
        when(repository.save(any(Filme.class))).thenAnswer(inv -> inv.getArgument(0));

        Filme salvo = service.importar("Inception", 2010);

        assertThat(salvo.getTitulo()).isEqualTo("Inception");
        verify(repository).save(any(Filme.class));
    }

    @Test
    void importarDoOmdbComDadosIncompletosRetornaDadosInvalidos() {
        // O OMDb pode devolver "N/A" (convertido para null) em campos obrigatórios.
        Filme incompleto = new Filme("Filme Obscuro", "Drama", 2001, null);
        when(omdbClient.buscarPorTitulo("Filme Obscuro", null)).thenReturn(incompleto);

        assertThrows(DadosInvalidosException.class, () -> service.importar("Filme Obscuro", null));

        verify(repository, never()).save(any());
    }
}
