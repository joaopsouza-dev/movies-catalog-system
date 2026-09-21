package br.com.catalogofilmes.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.catalogofilmes.model.Filme;
import br.com.catalogofilmes.model.ImportacaoRequisicao;
import br.com.catalogofilmes.service.FilmeService;

/**
 * Endpoints REST do catálogo. O controller só traduz HTTP; as regras ficam em {@link FilmeService}
 * e os erros são convertidos em respostas por TratadorDeErros.
 */
@RestController
@RequestMapping("/filmes")
public class FilmeController {

    private final FilmeService service;

    public FilmeController(FilmeService service) {
        this.service = service;
    }

    /** Cadastrar (CA1, CA2, CA3): 201, 400 ou 409. */
    @PostMapping
    public ResponseEntity<Filme> cadastrar(@RequestBody Filme filme) {
        Filme salvo = service.cadastrar(filme);
        return ResponseEntity.created(URI.create("/filmes/" + salvo.getId())).body(salvo);
    }

    /** Consultar todos (CA4): 200. */
    @GetMapping
    public List<Filme> listar() {
        return service.listar();
    }

    /** Consultar um filme (CA4): 200 ou 404. */
    @GetMapping("/{id}")
    public Filme buscar(@PathVariable("id") Long id) {
        return service.buscarPorId(id);
    }

    /** Alterar (CA5): 200, 400, 404 ou 409. */
    @PutMapping("/{id}")
    public Filme alterar(@PathVariable("id") Long id, @RequestBody Filme filme) {
        return service.alterar(id, filme);
    }

    /** Excluir (CA6): 204 ou 404. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable("id") Long id) {
        service.excluir(id);
    }

    /** Consulta o OMDb sem salvar: 200, 400, 404 ou 502. */
    @GetMapping("/omdb")
    public Filme consultarNoOmdb(@RequestParam(name = "titulo", required = false) String titulo,
                                 @RequestParam(name = "ano", required = false) Integer ano) {
        return service.consultarNoOmdb(titulo, ano);
    }

    /** Busca no OMDb e cadastra: 201, 400, 404, 409 ou 502. */
    @PostMapping("/importar")
    public ResponseEntity<Filme> importar(@RequestBody ImportacaoRequisicao requisicao) {
        Filme salvo = service.importar(requisicao.titulo(), requisicao.ano());
        return ResponseEntity.created(URI.create("/filmes/" + salvo.getId())).body(salvo);
    }
}
