package br.com.catalogofilmes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.catalogofilmes.model.Filme;

public interface FilmeRepository extends JpaRepository<Filme, Long> {

    List<Filme> findAllByOrderByTituloAsc();

    boolean existsByTituloIgnoreCaseAndAnoLancamento(String titulo, Integer anoLancamento);

    boolean existsByTituloIgnoreCaseAndAnoLancamentoAndIdNot(String titulo, Integer anoLancamento, Long id);
}
