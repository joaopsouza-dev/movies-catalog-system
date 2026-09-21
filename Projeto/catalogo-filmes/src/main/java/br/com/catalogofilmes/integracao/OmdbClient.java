package br.com.catalogofilmes.integracao;

import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.catalogofilmes.exception.FilmeNaoEncontradoException;
import br.com.catalogofilmes.exception.OmdbIndisponivelException;
import br.com.catalogofilmes.model.Filme;

/**
 * Cliente HTTP da API do OMDb (https://www.omdbapi.com/). Só faz a chamada e a conversão;
 * as regras do catálogo ficam no service.
 */
@Component
public class OmdbClient {

    private final RestTemplate restTemplate;
    private final String urlBase;
    private final String chaveApi;

    public OmdbClient(RestTemplate restTemplate,
                      @Value("${omdb.url}") String urlBase,
                      @Value("${omdb.api-key}") String chaveApi) {
        this.restTemplate = restTemplate;
        this.urlBase = urlBase;
        this.chaveApi = chaveApi;
    }

    /**
     * Busca um filme pelo título (e, opcionalmente, pelo ano) e converte para {@link Filme}.
     * O filme retornado NÃO é salvo e pode ter campos nulos quando o OMDb não os informa.
     *
     * @throws FilmeNaoEncontradoException se o OMDb não encontrar o filme
     * @throws OmdbIndisponivelException   se a chave não estiver configurada ou a consulta falhar
     */
    public Filme buscarPorTitulo(String titulo, Integer ano) {
        if (chaveApi == null || chaveApi.isBlank()) {
            throw new OmdbIndisponivelException(
                    "A chave da API do OMDb não foi configurada.");
        }

        Map<String, Object> variaveis = new HashMap<>();
        variaveis.put("chave", chaveApi);
        variaveis.put("titulo", titulo);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(urlBase)
                .queryParam("t", "{titulo}")
                .queryParam("apikey", "{chave}");
        if (ano != null) {
            builder.queryParam("y", "{ano}");
            variaveis.put("ano", ano);
        }
        // encode(): os valores das variáveis são codificados (espaços, "&" etc. não quebram a URL)
        URI uri = builder.encode().buildAndExpand(variaveis).toUri();

        OmdbResposta resposta;
        try {
            resposta = restTemplate.getForObject(uri, OmdbResposta.class);
        } catch (RestClientException e) {
            // A mensagem da exceção original é omitida de propósito: ela contém a URL com a chave.
            throw new OmdbIndisponivelException(
                    "Não foi possível consultar o OMDb (verifique a conexão e se a chave da API é válida).", e);
        }

        if (resposta == null) {
            throw new OmdbIndisponivelException("O OMDb devolveu uma resposta vazia.");
        }
        if (!"True".equalsIgnoreCase(resposta.resposta())) {
            String erro = resposta.erro() == null ? "" : resposta.erro();
            String erroMinusculo = erro.toLowerCase(Locale.ROOT);
            if (erroMinusculo.contains("not found") || erroMinusculo.contains("incorrect imdb id")) {
                throw new FilmeNaoEncontradoException("Filme não encontrado no OMDb.");
            }
            throw new OmdbIndisponivelException("O OMDb recusou a consulta: " + erro);
        }
        return OmdbConversor.paraFilme(resposta);
    }
}
