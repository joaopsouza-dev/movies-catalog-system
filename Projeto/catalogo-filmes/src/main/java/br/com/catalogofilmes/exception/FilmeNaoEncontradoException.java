package br.com.catalogofilmes.exception;

/** Filme não encontrado no catálogo ou no OMDb (HTTP 404). */
public class FilmeNaoEncontradoException extends CatalogoException {

    public FilmeNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
