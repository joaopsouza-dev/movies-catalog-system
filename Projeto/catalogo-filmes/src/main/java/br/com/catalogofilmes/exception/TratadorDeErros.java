package br.com.catalogofilmes.exception;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Converte as exceções em respostas HTTP com o código adequado e uma mensagem clara (CA9, RAP003).
 */
@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(DadosInvalidosException.class)
    public ResponseEntity<ErroResposta> dadosInvalidos(DadosInvalidosException e) {
        return resposta(HttpStatus.BAD_REQUEST, e.getMessage(), e.getErros());
    }

    @ExceptionHandler(FilmeNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> naoEncontrado(FilmeNaoEncontradoException e) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage(), List.of());
    }

    @ExceptionHandler(FilmeDuplicadoException.class)
    public ResponseEntity<ErroResposta> duplicado(FilmeDuplicadoException e) {
        return resposta(HttpStatus.CONFLICT, e.getMessage(), List.of());
    }

    @ExceptionHandler(OmdbIndisponivelException.class)
    public ResponseEntity<ErroResposta> omdbIndisponivel(OmdbIndisponivelException e) {
        return resposta(HttpStatus.BAD_GATEWAY, e.getMessage(), List.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> corpoInvalido(HttpMessageNotReadableException e) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Corpo da requisição ausente, mal formatado ou com tipos inválidos.", List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResposta> parametroInvalido(MethodArgumentTypeMismatchException e) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro '" + e.getName() + "'.", List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> violacaoDeIntegridade(DataIntegrityViolationException e) {
        return resposta(HttpStatus.CONFLICT,
                "Violação de integridade dos dados (possível filme duplicado).", List.of());
    }

    private ResponseEntity<ErroResposta> resposta(HttpStatus status, String mensagem, List<String> detalhes) {
        return ResponseEntity.status(status).body(new ErroResposta(mensagem, detalhes));
    }
}
