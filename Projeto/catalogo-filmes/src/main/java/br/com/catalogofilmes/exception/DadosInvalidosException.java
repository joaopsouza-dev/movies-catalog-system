package br.com.catalogofilmes.exception;

import java.util.List;

/** Dados de entrada inválidos (HTTP 400). Guarda a lista de problemas encontrados. */
public class DadosInvalidosException extends CatalogoException {

    private final List<String> erros;

    public DadosInvalidosException(String mensagem, List<String> erros) {
        super(mensagem);
        this.erros = List.copyOf(erros);
    }

    public DadosInvalidosException(String mensagem) {
        this(mensagem, List.of());
    }

    public List<String> getErros() {
        return erros;
    }
}
