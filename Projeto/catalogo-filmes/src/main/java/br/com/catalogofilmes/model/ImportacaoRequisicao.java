package br.com.catalogofilmes.model;

/**
 * Corpo da requisição de importação de um filme do OMDb.
 *
 * @param titulo título do filme a buscar (obrigatório)
 * @param ano    ano de lançamento, para desambiguar títulos repetidos (opcional)
 */
public record ImportacaoRequisicao(String titulo, Integer ano) {
}
