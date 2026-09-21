package br.com.catalogofilmes.exception;

import java.util.List;

/**
 * Formato padrão das respostas de erro da API (CA9).
 *
 * @param erro     mensagem principal
 * @param detalhes lista de problemas específicos (pode ser vazia)
 */
public record ErroResposta(String erro, List<String> detalhes) {
}
