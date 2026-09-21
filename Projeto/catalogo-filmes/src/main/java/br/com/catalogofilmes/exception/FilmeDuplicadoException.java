package br.com.catalogofilmes.exception;

/** Já existe um filme com o mesmo título e ano de lançamento (HTTP 409). */
public class FilmeDuplicadoException extends CatalogoException {

    public FilmeDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
