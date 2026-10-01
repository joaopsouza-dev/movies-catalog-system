package br.com.catalogofilmes.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.catalogofilmes.exception.FilmeNaoEncontradoException;
import br.com.catalogofilmes.integracao.OmdbClient;
import br.com.catalogofilmes.model.Filme;
import br.com.catalogofilmes.repository.FilmeRepository;

/**
 * Testes de API (CT05 a CT09 e cenários complementares). Sobem o contexto completo do Spring com o
 * banco H2 e simulam requisições HTTP com MockMvc. Só o cliente do OMDb é simulado, para os testes
 * não dependerem de internet nem gastarem o limite diário da chave.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:api-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class FilmeApiIntegracaoTest {

    private static final String MATRIX_JSON =
            "{\"titulo\":\"Matrix\",\"genero\":\"Ficcao\",\"anoLancamento\":1999,\"duracao\":136}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FilmeRepository repository;

    @MockitoBean
    private OmdbClient omdbClient;

    @BeforeEach
    void limparBanco() {
        repository.deleteAll();
    }

    private Filme salvar(String titulo, String genero, int ano, int duracao) {
        return repository.save(new Filme(titulo, genero, ano, duracao));
    }

    // ------------------------------------------------------------ CT05

    /** CT05 - POST /filmes com JSON válido retorna 201 Created. */
    @Test
    void cadastrarFilmePelaApi() throws Exception {
        mockMvc.perform(post("/filmes").contentType(MediaType.APPLICATION_JSON).content(MATRIX_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/filmes/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titulo").value("Matrix"))
                .andExpect(jsonPath("$.genero").value("Ficcao"))
                .andExpect(jsonPath("$.anoLancamento").value(1999))
                .andExpect(jsonPath("$.duracao").value(136));
    }

    @Test
    void cadastrarFilmeDuplicadoPelaApiRetorna409() throws Exception {
        salvar("Matrix", "Ficcao", 1999, 136);

        mockMvc.perform(post("/filmes").contentType(MediaType.APPLICATION_JSON).content(MATRIX_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").exists());
    }

    // ------------------------------------------------------------ CT06

    /** CT06 - POST /filmes com campos inválidos retorna 400 Bad Request. */
    @Test
    void cadastrarFilmeInvalidoPelaApi() throws Exception {
        String semTituloENemGenero =
                "{\"titulo\":\"\",\"genero\":\"\",\"anoLancamento\":1999,\"duracao\":136}";

        mockMvc.perform(post("/filmes").contentType(MediaType.APPLICATION_JSON).content(semTituloENemGenero))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Dados do filme inválidos."))
                .andExpect(jsonPath("$.detalhes", hasSize(2)))
                .andExpect(jsonPath("$.detalhes[0]").value("O título é obrigatório."));
    }

    @Test
    void cadastrarComCorpoMalFormatadoRetorna400() throws Exception {
        mockMvc.perform(post("/filmes").contentType(MediaType.APPLICATION_JSON).content("{isso nao e json"))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------ CT07

    /** CT07 - GET /filmes retorna 200 OK e a lista (ordenada por título). */
    @Test
    void listarFilmesPelaApi() throws Exception {
        salvar("Matrix", "Ficcao", 1999, 136);
        salvar("Avatar", "Aventura", 2009, 162);

        mockMvc.perform(get("/filmes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].titulo").value("Avatar"))
                .andExpect(jsonPath("$[1].titulo").value("Matrix"));
    }

    @Test
    void listarSemFilmesRetornaListaVazia() throws Exception {
        mockMvc.perform(get("/filmes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void buscarFilmePorIdPelaApi() throws Exception {
        Filme matrix = salvar("Matrix", "Ficcao", 1999, 136);

        mockMvc.perform(get("/filmes/{id}", matrix.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(matrix.getId()))
                .andExpect(jsonPath("$.titulo").value("Matrix"));
    }

    @Test
    void buscarFilmeInexistentePelaApi() throws Exception {
        mockMvc.perform(get("/filmes/{id}", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    void buscarComIdNaoNumericoRetorna400() throws Exception {
        mockMvc.perform(get("/filmes/abc"))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------ CT08

    /** CT08 - PUT /filmes/{id} com dados válidos retorna 200 OK e os dados atualizados. */
    @Test
    void alterarFilmePelaApi() throws Exception {
        Filme matrix = salvar("Matrix", "Ficcao", 1999, 136);
        String novosDados =
                "{\"titulo\":\"Matrix Reloaded\",\"genero\":\"Acao\",\"anoLancamento\":2003,\"duracao\":138}";

        mockMvc.perform(put("/filmes/{id}", matrix.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(novosDados))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(matrix.getId()))
                .andExpect(jsonPath("$.titulo").value("Matrix Reloaded"))
                .andExpect(jsonPath("$.genero").value("Acao"))
                .andExpect(jsonPath("$.anoLancamento").value(2003))
                .andExpect(jsonPath("$.duracao").value(138));
    }

    @Test
    void alterarFilmeInexistentePelaApiRetorna404() throws Exception {
        mockMvc.perform(put("/filmes/{id}", 99999)
                        .contentType(MediaType.APPLICATION_JSON).content(MATRIX_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void alterarComDadosInvalidosPelaApiRetorna400() throws Exception {
        Filme matrix = salvar("Matrix", "Ficcao", 1999, 136);
        String invalido = "{\"titulo\":\"\",\"genero\":\"Acao\",\"anoLancamento\":2003,\"duracao\":138}";

        mockMvc.perform(put("/filmes/{id}", matrix.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void alterarParaTituloEAnoDeOutroFilmeRetorna409() throws Exception {
        Filme matrix = salvar("Matrix", "Ficcao", 1999, 136);
        salvar("Inception", "Ficcao", 2010, 148);
        String conflito =
                "{\"titulo\":\"Inception\",\"genero\":\"Ficcao\",\"anoLancamento\":2010,\"duracao\":148}";

        mockMvc.perform(put("/filmes/{id}", matrix.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(conflito))
                .andExpect(status().isConflict());
    }

    // ------------------------------------------------------------ CT09

    /** CT09 - DELETE /filmes/{id} com ID existente retorna 204 No Content. */
    @Test
    void excluirFilmePelaApi() throws Exception {
        Filme matrix = salvar("Matrix", "Ficcao", 1999, 136);

        mockMvc.perform(delete("/filmes/{id}", matrix.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/filmes/{id}", matrix.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void excluirFilmeInexistentePelaApiRetorna404() throws Exception {
        mockMvc.perform(delete("/filmes/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------ OMDb (cliente simulado)

    @Test
    void consultarFilmeNoOmdbPelaApiNaoSalvaNoBanco() throws Exception {
        when(omdbClient.buscarPorTitulo("Inception", null))
                .thenReturn(new Filme("Inception", "Action, Adventure, Sci-Fi", 2010, 148));

        mockMvc.perform(get("/filmes/omdb").param("titulo", "Inception"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Inception"))
                .andExpect(jsonPath("$.anoLancamento").value(2010))
                .andExpect(jsonPath("$.duracao").value(148));

        mockMvc.perform(get("/filmes"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void consultarNoOmdbSemTituloRetorna400() throws Exception {
        mockMvc.perform(get("/filmes/omdb"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultarFilmeInexistenteNoOmdbRetorna404() throws Exception {
        when(omdbClient.buscarPorTitulo("Xyzxyz", null))
                .thenThrow(new FilmeNaoEncontradoException("Filme não encontrado no OMDb."));

        mockMvc.perform(get("/filmes/omdb").param("titulo", "Xyzxyz"))
                .andExpect(status().isNotFound());
    }

    @Test
    void importarFilmeDoOmdbPelaApi() throws Exception {
        when(omdbClient.buscarPorTitulo("Inception", 2010))
                .thenReturn(new Filme("Inception", "Action, Adventure, Sci-Fi", 2010, 148));

        mockMvc.perform(post("/filmes/importar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Inception\",\"ano\":2010}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titulo").value("Inception"));

        mockMvc.perform(get("/filmes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo").value("Inception"));
    }

    @Test
    void importarFilmeJaCadastradoRetorna409() throws Exception {
        salvar("Inception", "Action, Adventure, Sci-Fi", 2010, 148);
        when(omdbClient.buscarPorTitulo("Inception", 2010))
                .thenReturn(new Filme("Inception", "Action, Adventure, Sci-Fi", 2010, 148));

        mockMvc.perform(post("/filmes/importar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Inception\",\"ano\":2010}"))
                .andExpect(status().isConflict());
    }
}
