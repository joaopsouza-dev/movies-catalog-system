package br.com.catalogofilmes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import br.com.catalogofilmes.model.Filme;
import br.com.catalogofilmes.repository.FilmeRepository;
import br.com.catalogofilmes.service.FilmeService;

/**
 * Testes com FALHAS PROPOSITAIS, para demonstrar que as ferramentas de teste detectam
 * comportamentos divergentes. Cada teste tem um resultado esperado INCORRETO de propósito.
 *
 * <p>Ficam desligados por padrão (para o build normal continuar verde). Para vê-los falhar:
 * <pre>mvn test -DfalhasPropositais=true -Dtest=FalhasPropositaisTest</pre>
 */
@EnabledIfSystemProperty(named = "falhasPropositais", matches = "true")
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:falhas;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class FalhasPropositaisTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FilmeService service;

    @Autowired
    private FilmeRepository repository;

    @BeforeEach
    void limparBanco() {
        repository.deleteAll();
    }

    /** FALHA PROPOSITAL (unitário): o título salvo é "Matrix", mas o teste espera "Matrix Reloaded". */
    @Test
    void falhaPropositalUnitaria() {
        Filme salvo = service.cadastrar(new Filme("Matrix", "Ficcao", 1999, 136));

        assertThat(salvo.getTitulo()).isEqualTo("Matrix Reloaded");
    }

    /** FALHA PROPOSITAL (API): o cadastro devolve 201 Created, mas o teste espera 200 OK. */
    @Test
    void falhaPropositalDeApi() throws Exception {
        String json = "{\"titulo\":\"Matrix\",\"genero\":\"Ficcao\",\"anoLancamento\":1999,\"duracao\":136}";

        mockMvc.perform(post("/filmes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk());
    }
}
