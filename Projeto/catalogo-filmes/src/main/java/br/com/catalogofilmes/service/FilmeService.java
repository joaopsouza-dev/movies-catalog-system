package br.com.catalogofilmes.service;

import java.time.Clock;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.catalogofilmes.exception.DadosInvalidosException;
import br.com.catalogofilmes.exception.FilmeDuplicadoException;
import br.com.catalogofilmes.exception.FilmeNaoEncontradoException;
import br.com.catalogofilmes.integracao.OmdbClient;
import br.com.catalogofilmes.model.Filme;
import br.com.catalogofilmes.repository.FilmeRepository;

/**
 * Regras de negócio do catálogo de filmes.
 *
 * <p>Regras adotadas (a História de Usuário cita RN001 a RN008 sem detalhá-las):
 * <ul>
 *   <li>RN001 - título obrigatório, até 100 caracteres;</li>
 *   <li>RN002 - gênero obrigatório, até 50 caracteres;</li>
 *   <li>RN003 - ano de lançamento obrigatório, de 1888 até (ano atual + 5);</li>
 *   <li>RN004 - duração obrigatória, de 1 a 9999 minutos;</li>
 *   <li>RN005 - não pode existir outro filme com o mesmo título (sem diferenciar maiúsculas) e ano;</li>
 *   <li>RN006 - alterar aplica as mesmas validações, ignorando o próprio filme na checagem de duplicidade;</li>
 *   <li>RN007 - consultar retorna os filmes ordenados por título;</li>
 *   <li>RN008 - excluir exige que o filme exista (a confirmação do usuário é feita na interface).</li>
 * </ul>
 */
@Service
public class FilmeService {

    public static final int ANO_MINIMO = 1888;
    public static final int ANOS_FUTUROS_PERMITIDOS = 5;
    public static final int DURACAO_MINIMA = 1;
    public static final int DURACAO_MAXIMA = 9999;

    private final FilmeRepository repository;
    private final OmdbClient omdbClient;
    private final Clock relogio;

    public FilmeService(FilmeRepository repository, OmdbClient omdbClient, Clock relogio) {
        this.repository = repository;
        this.omdbClient = omdbClient;
        this.relogio = relogio;
    }

    /** CA1, CA2, CA3. */
    public Filme cadastrar(Filme filme) {
        Filme novo = normalizar(filme);
        validar(novo);
        if (repository.existsByTituloIgnoreCaseAndAnoLancamento(novo.getTitulo(), novo.getAnoLancamento())) {
            throw new FilmeDuplicadoException(mensagemDuplicado(novo));
        }
        return repository.save(novo);
    }

    /** CA4. */
    public List<Filme> listar() {
        return repository.findAllByOrderByTituloAsc();
    }

    /** CA4. */
    public Filme buscarPorId(Long id) {
        if (id == null) {
            throw new DadosInvalidosException("O id do filme é obrigatório.");
        }
        return repository.findById(id)
                .orElseThrow(() -> new FilmeNaoEncontradoException("Filme com id " + id + " não encontrado."));
    }

    /** CA5. */
    public Filme alterar(Long id, Filme dados) {
        Filme existente = buscarPorId(id);
        Filme novo = normalizar(dados);
        validar(novo);
        if (repository.existsByTituloIgnoreCaseAndAnoLancamentoAndIdNot(
                novo.getTitulo(), novo.getAnoLancamento(), id)) {
            throw new FilmeDuplicadoException(mensagemDuplicado(novo));
        }
        existente.setTitulo(novo.getTitulo());
        existente.setGenero(novo.getGenero());
        existente.setAnoLancamento(novo.getAnoLancamento());
        existente.setDuracao(novo.getDuracao());
        return repository.save(existente);
    }

    /** CA6 (a confirmação do usuário é responsabilidade da interface). */
    public void excluir(Long id) {
        Filme filme = buscarPorId(id);
        repository.delete(filme);
    }

    /** Consulta o OMDb sem salvar nada. */
    public Filme consultarNoOmdb(String titulo, Integer ano) {
        if (titulo == null || titulo.isBlank()) {
            throw new DadosInvalidosException("O título é obrigatório para consultar o OMDb.");
        }
        return omdbClient.buscarPorTitulo(titulo.trim(), ano);
    }

    /** Consulta o OMDb e cadastra o filme aplicando as mesmas regras do cadastro manual. */
    public Filme importar(String titulo, Integer ano) {
        Filme encontrado = consultarNoOmdb(titulo, ano);
        try {
            return cadastrar(encontrado);
        } catch (DadosInvalidosException e) {
            throw new DadosInvalidosException(
                    "Os dados retornados pelo OMDb estão incompletos ou inválidos para o cadastro.",
                    e.getErros());
        }
    }

    // ------------------------------------------------------------------ auxiliares

    /** Copia o filme (descartando o id informado) e remove espaços das pontas dos textos. */
    private Filme normalizar(Filme filme) {
        if (filme == null) {
            throw new DadosInvalidosException("Os dados do filme não foram informados.");
        }
        return new Filme(
                aparar(filme.getTitulo()),
                aparar(filme.getGenero()),
                filme.getAnoLancamento(),
                filme.getDuracao());
    }

    private static String aparar(String texto) {
        return texto == null ? null : texto.trim();
    }

    private void validar(Filme filme) {
        List<String> erros = new ArrayList<>();

        if (filme.getTitulo() == null || filme.getTitulo().isEmpty()) {
            erros.add("O título é obrigatório.");
        } else if (filme.getTitulo().length() > Filme.TITULO_MAX) {
            erros.add("O título deve ter no máximo " + Filme.TITULO_MAX + " caracteres.");
        }

        if (filme.getGenero() == null || filme.getGenero().isEmpty()) {
            erros.add("O gênero é obrigatório.");
        } else if (filme.getGenero().length() > Filme.GENERO_MAX) {
            erros.add("O gênero deve ter no máximo " + Filme.GENERO_MAX + " caracteres.");
        }

        Integer ano = filme.getAnoLancamento();
        int anoMaximo = Year.now(relogio).getValue() + ANOS_FUTUROS_PERMITIDOS;
        if (ano == null) {
            erros.add("O ano de lançamento é obrigatório.");
        } else if (ano < ANO_MINIMO || ano > anoMaximo) {
            erros.add("O ano de lançamento deve estar entre " + ANO_MINIMO + " e " + anoMaximo + ".");
        }

        Integer duracao = filme.getDuracao();
        if (duracao == null) {
            erros.add("A duração é obrigatória.");
        } else if (duracao < DURACAO_MINIMA || duracao > DURACAO_MAXIMA) {
            erros.add("A duração deve estar entre " + DURACAO_MINIMA + " e " + DURACAO_MAXIMA + " minutos.");
        }

        if (!erros.isEmpty()) {
            throw new DadosInvalidosException("Dados do filme inválidos.", erros);
        }
    }

    private static String mensagemDuplicado(Filme filme) {
        return "Já existe um filme \"" + filme.getTitulo() + "\" com ano de lançamento "
                + filme.getAnoLancamento() + ".";
    }
}
