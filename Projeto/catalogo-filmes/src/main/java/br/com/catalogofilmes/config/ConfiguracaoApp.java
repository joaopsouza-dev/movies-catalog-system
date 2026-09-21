package br.com.catalogofilmes.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ConfiguracaoApp {

    /**
     * Relógio injetável: permite fixar a data nos testes (validação do ano de lançamento).
     */
    @Bean
    public Clock relogio() {
        return Clock.systemDefaultZone();
    }

    /**
     * Cliente HTTP usado para chamar o OMDb, com limite de tempo para não travar a aplicação.
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(5000);
        fabrica.setReadTimeout(5000);
        return new RestTemplate(fabrica);
    }
}
