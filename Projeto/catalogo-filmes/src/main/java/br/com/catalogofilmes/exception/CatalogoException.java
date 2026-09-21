package br.com.catalogofilmes.exception;

/**
 * Base de todas as exceções de negócio do catálogo. Permite que a API (TratadorDeErros)
 * e a CLI tratem os erros de forma uniforme.
 */
public class CatalogoException extends RuntimeException {

    public CatalogoException(String mensagem) {
        super(mensagem);
    }

    public CatalogoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
