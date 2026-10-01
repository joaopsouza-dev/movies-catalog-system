package br.com.catalogofilmes.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.catalogofilmes.cli.MenuCli;
import br.com.catalogofilmes.model.Filme;
import br.com.catalogofilmes.repository.FilmeRepository;
import br.com.catalogofilmes.service.FilmeService;

/**
 * Testes E2E (CT10 a CT12) pela interface de linha de comando.
 *
 * <p>Um "usuário" digita as opções do menu (entrada simulada) e o teste confere o que a CLI mostra
 * na tela. Todas as camadas são reais: CLI, service, repository e banco H2.
 * As entradas usam só caracteres ASCII para não depender da codificação padrão do sistema.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:e2e-cli;DB_CLOSE_DELAY=-1")
class FilmeE2ECliTest {

    @Autowired
    private FilmeService service;

    @Autowired
    private FilmeRepository repository;

    @BeforeEach
    void limparBanco() {
        repository.deleteAll();
    }

    /** Simula uma sessão do usuário na CLI e devolve tudo o que foi exibido. */
    private String sessao(String... linhas) {
        String entrada = String.join("\n", linhas) + "\n";
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream saida = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        new MenuCli(service, new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)), saida)
                .executar();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private Long cadastrarMatrixPelaCli() {
        sessao("3", "Matrix", "Ficcao", "1999", "136", "0");
        return repository.findAll().get(0).getId();
    }

    /** CT10 - usuário cadastra um filme e depois consulta a listagem. */
    @Test
    void fluxoCompletoDeCadastroEConsulta() {
        String tela = sessao(
                "3", "Matrix", "Ficcao", "1999", "136", // cadastrar
                "1",                                      // listar
                "0");                                     // sair

        assertThat(tela).contains("Filme cadastrado com sucesso!");
        String aposCadastro = tela.substring(tela.indexOf("Filme cadastrado com sucesso!"));
        assertThat(aposCadastro).contains("Matrix").contains("Ficcao").contains("1999").contains("136 min");
        assertThat(repository.findAll()).hasSize(1);
    }

    /** CT11 - usuário cadastra um filme e altera seus dados. */
    @Test
    void fluxoCompletoDeAlteracao() {
        Long id = cadastrarMatrixPelaCli();

        String tela = sessao(
                "4", String.valueOf(id),                              // alterar
                "Matrix Reloaded", "Acao", "2003", "138",             // novos dados
                "1",                                                  // listar
                "0");

        assertThat(tela).contains("Filme alterado com sucesso!");
        String aposAlteracao = tela.substring(tela.indexOf("Filme alterado com sucesso!"));
        assertThat(aposAlteracao).contains("Matrix Reloaded").contains("Acao")
                .contains("2003").contains("138 min");
        assertThat(aposAlteracao).doesNotContain("1999");

        Filme noBanco = repository.findById(id).orElseThrow();
        assertThat(noBanco.getTitulo()).isEqualTo("Matrix Reloaded");
        assertThat(noBanco.getAnoLancamento()).isEqualTo(2003);
    }

    /** CT12 - usuário cadastra um filme e depois o exclui. */
    @Test
    void fluxoCompletoDeExclusao() {
        Long id = cadastrarMatrixPelaCli();

        String tela = sessao(
                "5", String.valueOf(id), "s",   // excluir e confirmar
                "1",                            // listar
                "0");

        assertThat(tela).contains("Filme exclu");
        assertThat(tela).contains("Nenhum filme cadastrado.");
        assertThat(repository.findById(id)).isEmpty();
    }

    @Test
    void exclusaoCanceladaMantemOFilme() {
        Long id = cadastrarMatrixPelaCli();

        String tela = sessao("5", String.valueOf(id), "n", "0");

        assertThat(tela).contains("Exclus");
        assertThat(repository.findById(id)).isPresent();
    }

    @Test
    void cadastroInvalidoMostraErroNaTela() {
        String tela = sessao("3", "", "Ficcao", "1999", "136", "0");

        assertThat(tela).contains("Erro:").contains("obrigat");
        assertThat(repository.findAll()).isEmpty();
    }
}
