package br.com.catalogofilmes.exception;

/** Falha ao consultar o OMDb: sem rede, chave inválida, limite diário atingido etc. (HTTP 502). */
public class OmdbIndisponivelException extends CatalogoException {

    public OmdbIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public OmdbIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
