package br.com.catalogofilmes.integracao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Resposta JSON do OMDb. Os campos vêm em inglês e com inicial maiúscula.
 * Quando "Response" é "False", o campo "Error" explica o motivo.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OmdbResposta(
        @JsonProperty("Title") String titulo,
        @JsonProperty("Year") String ano,
        @JsonProperty("Runtime") String duracao,
        @JsonProperty("Genre") String genero,
        @JsonProperty("imdbID") String imdbId,
        @JsonProperty("Response") String resposta,
        @JsonProperty("Error") String erro) {
}
