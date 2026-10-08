package br.com.catalogofilmes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.catalogofilmes.controller.FilmeController;
import br.com.catalogofilmes.integracao.OmdbClient;
import br.com.catalogofilmes.model.Filme;
import br.com.catalogofilmes.repository.FilmeRepository;
import br.com.catalogofilmes.service.FilmeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Testes com FALHAS PROPOSITAIS, para demonstrar que as ferramentas de teste detectam
 * comportamentos divergentes. Cada teste tem um resultado esperado INCORRETO de propósito.
 *
 * <p>Ficam desligados por padrão (para o build normal continuar verde). Para vê-los falhar:
 * <pre>mvn test -DfalhasPropositais=true -Dtest=FalhasPropositaisTest</pre>
 */
@ExtendWith(MockitoExtension.class)
@EnabledIfSystemProperty(named = "falhasPropositais", matches = "true")
class FalhasPropositaisTest {

    private FilmeService service;

    @Mock
    private FilmeRepository repository;

    @Mock
    private OmdbClient omdbClient;

    @BeforeEach
    void setUp() {
        Clock relogio = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneOffset.UTC);
        service = new FilmeService(repository, omdbClient, relogio);
    }

    /** FALHA PROPOSITAL (unitário no Serviço): o título salvo é "Matrix", mas o teste exige "Matrix Reloaded". */
    @Test
    void falhaPropositalUnitaria() {
        Filme filmeDeEntrada = new Filme("Matrix", "Ficcao", 1999, 136);

        when(repository.save(any(Filme.class))).thenAnswer(invocacao -> {
            Filme f = invocacao.getArgument(0);
            f.setId(1L);
            return f;
        });

        Filme salvo = service.cadastrar(filmeDeEntrada);

        // EXPECTATIVA ERRADA DE PROPÓSITO
        assertThat(salvo.getTitulo()).isEqualTo("Matrix Reloaded");
    }

    /** FALHA PROPOSITAL (unitário no Controller): o controller devolve 201 Created, mas o teste exige 200 OK. */
    @Test
    void falhaPropositalDeApi() {
        // Criamos a classe do controller manualmente passando o serviço de teste
        FilmeController controller = new FilmeController(service);

        Filme filme = new Filme("Matrix", "Ficcao", 1999, 136);

        when(repository.save(any(Filme.class))).thenAnswer(invocacao -> {
            Filme f = invocacao.getArgument(0);
            f.setId(1L);
            return f;
        });

        ResponseEntity<Filme> resposta = controller.cadastrar(filme);

        // EXPECTATIVA ERRADA DE PROPÓSITO: O status será 201 (CREATED), mas esperamos 200 (OK)
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}