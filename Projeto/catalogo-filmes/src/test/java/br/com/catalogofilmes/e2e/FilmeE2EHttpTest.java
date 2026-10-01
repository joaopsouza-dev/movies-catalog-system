package br.com.catalogofilmes.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.catalogofilmes.repository.FilmeRepository;

/**
 * Testes E2E (CT10 a CT12) pela rede: a aplicação sobe de verdade em uma porta aleatória e o
 * "usuário" é um cliente HTTP real (java.net.http), sem MockMvc. Complementa os E2E da CLI.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:e2e-http;DB_CLOSE_DELAY=-1")
class FilmeE2EHttpTest {

    private static final String MATRIX_JSON =
            "{\"titulo\":\"Matrix\",\"genero\":\"Ficcao\",\"anoLancamento\":1999,\"duracao\":136}";

    @Value("${local.server.port}")
    private int porta;

    @Autowired
    private FilmeRepository repository;

    private final HttpClient cliente = HttpClient.newHttpClient();

    @BeforeEach
    void limparBanco() {
        repository.deleteAll();
    }

    private HttpResponse<String> enviar(String metodo, String caminho, String corpo) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho))
                .header("Content-Type", "application/json");
        HttpRequest.BodyPublisher body = corpo == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(corpo);
        return cliente.send(req.method(metodo, body).build(), HttpResponse.BodyHandlers.ofString());
    }

    private long cadastrarMatrix() throws Exception {
        HttpResponse<String> resposta = enviar("POST", "/filmes", MATRIX_JSON);
        assertThat(resposta.statusCode()).isEqualTo(201);
        Matcher m = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(resposta.body());
        assertThat(m.find()).isTrue();
        return Long.parseLong(m.group(1));
    }

    /** CT10 - cadastra um filme e depois consulta a listagem. */
    @Test
    void fluxoCompletoDeCadastroEConsulta() throws Exception {
        cadastrarMatrix();

        HttpResponse<String> lista = enviar("GET", "/filmes", null);

        assertThat(lista.statusCode()).isEqualTo(200);
        assertThat(lista.body()).contains("Matrix").contains("1999");
    }

    /** CT11 - cadastra um filme e altera seus dados. */
    @Test
    void fluxoCompletoDeAlteracao() throws Exception {
        long id = cadastrarMatrix();

        HttpResponse<String> alteracao = enviar("PUT", "/filmes/" + id,
                "{\"titulo\":\"Matrix Reloaded\",\"genero\":\"Acao\",\"anoLancamento\":2003,\"duracao\":138}");
        HttpResponse<String> consulta = enviar("GET", "/filmes/" + id, null);

        assertThat(alteracao.statusCode()).isEqualTo(200);
        assertThat(consulta.statusCode()).isEqualTo(200);
        assertThat(consulta.body()).contains("Matrix Reloaded").contains("2003").contains("138");
        assertThat(consulta.body()).doesNotContain("1999");
    }

    /** CT12 - cadastra um filme e depois o exclui. */
    @Test
    void fluxoCompletoDeExclusao() throws Exception {
        long id = cadastrarMatrix();

        HttpResponse<String> exclusao = enviar("DELETE", "/filmes/" + id, null);
        HttpResponse<String> lista = enviar("GET", "/filmes", null);
        HttpResponse<String> consulta = enviar("GET", "/filmes/" + id, null);

        assertThat(exclusao.statusCode()).isEqualTo(204);
        assertThat(lista.body()).doesNotContain("Matrix");
        assertThat(consulta.statusCode()).isEqualTo(404);
    }
}
