package br.com.catalogofilmes.cli;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import br.com.catalogofilmes.exception.CatalogoException;
import br.com.catalogofilmes.exception.DadosInvalidosException;
import br.com.catalogofilmes.model.Filme;
import br.com.catalogofilmes.service.FilmeService;

/**
 * Interface de linha de comando para demonstrar o funcionamento do catálogo.
 * Usa o mesmo {@link FilmeService} da API REST. Recebe entrada e saída por parâmetro
 * para poder ser testada sem console real.
 */
public class MenuCli {

    private static final String NAO_INFORMADO = "(não informado)";

    private final FilmeService service;
    private final Scanner entrada;
    private final PrintStream saida;

    public MenuCli(FilmeService service, InputStream entrada, PrintStream saida) {
        this.service = service;
        this.entrada = new Scanner(entrada);
        this.saida = saida;
    }

    public void executar() {
        boolean continuar = true;
        while (continuar) {
            exibirMenu();
            String opcao = lerLinha("Escolha uma opção: ");
            if (opcao == null) {
                break; // fim da entrada (ex.: Ctrl+D)
            }
            try {
                continuar = tratarOpcao(opcao.trim());
            } catch (DadosInvalidosException e) {
                saida.println("Erro: " + e.getMessage());
                for (String erro : e.getErros()) {
                    saida.println("  - " + erro);
                }
            } catch (CatalogoException e) {
                saida.println("Erro: " + e.getMessage());
            }
        }
        saida.println("Até logo!");
    }

    // ------------------------------------------------------------------ menu

    private void exibirMenu() {
        saida.println();
        saida.println("===== Catálogo de Filmes =====");
        saida.println("1 - Listar filmes");
        saida.println("2 - Buscar filme por ID");
        saida.println("3 - Cadastrar filme");
        saida.println("4 - Alterar filme");
        saida.println("5 - Excluir filme");
        saida.println("6 - Consultar filme no OMDb (não salva)");
        saida.println("7 - Importar filme do OMDb (salva no catálogo)");
        saida.println("0 - Sair");
    }

    /** @return false quando o usuário escolhe sair */
    private boolean tratarOpcao(String opcao) {
        switch (opcao) {
            case "1" -> listar();
            case "2" -> buscarPorId();
            case "3" -> cadastrar();
            case "4" -> alterar();
            case "5" -> excluir();
            case "6" -> consultarNoOmdb();
            case "7" -> importarDoOmdb();
            case "0" -> {
                return false;
            }
            default -> saida.println("Opção inválida.");
        }
        return true;
    }

    // ------------------------------------------------------------------ operações

    private void listar() {
        List<Filme> filmes = service.listar();
        if (filmes.isEmpty()) {
            saida.println("Nenhum filme cadastrado.");
            return;
        }
        saida.printf("%-5s %-40s %-25s %-6s %s%n", "ID", "TÍTULO", "GÊNERO", "ANO", "DURAÇÃO");
        for (Filme f : filmes) {
            saida.printf("%-5s %-40.40s %-25.25s %-6s %s%n",
                    f.getId(), f.getTitulo(), f.getGenero(),
                    valor(f.getAnoLancamento()), duracao(f));
        }
    }

    private void buscarPorId() {
        Long id = lerId("ID do filme: ");
        imprimirFilme(service.buscarPorId(id));
    }

    private void cadastrar() {
        String titulo = lerLinha("Título: ");
        String genero = lerLinha("Gênero: ");
        Integer ano = lerInteiro("Ano de lançamento: ");
        Integer duracao = lerInteiro("Duração (minutos): ");

        Filme salvo = service.cadastrar(new Filme(titulo, genero, ano, duracao));
        saida.println("Filme cadastrado com sucesso! ID: " + salvo.getId());
    }

    private void alterar() {
        Long id = lerId("ID do filme a alterar: ");
        Filme atual = service.buscarPorId(id);
        saida.println("Deixe em branco para manter o valor atual.");

        String titulo = lerOuManter("Título", atual.getTitulo());
        String genero = lerOuManter("Gênero", atual.getGenero());
        Integer ano = lerInteiroOuManter("Ano de lançamento", atual.getAnoLancamento());
        Integer duracao = lerInteiroOuManter("Duração (minutos)", atual.getDuracao());

        Filme alterado = service.alterar(id, new Filme(titulo, genero, ano, duracao));
        saida.println("Filme alterado com sucesso!");
        imprimirFilme(alterado);
    }

    private void excluir() {
        Long id = lerId("ID do filme a excluir: ");
        Filme filme = service.buscarPorId(id);
        String resposta = lerLinha("Tem certeza que deseja excluir \"" + filme.getTitulo() + "\"? (s/n): ");
        if (resposta != null
                && (resposta.trim().equalsIgnoreCase("s") || resposta.trim().equalsIgnoreCase("sim"))) {
            service.excluir(id);
            saida.println("Filme excluído com sucesso!");
        } else {
            saida.println("Exclusão cancelada.");
        }
    }

    private void consultarNoOmdb() {
        String titulo = lerLinha("Título do filme (em inglês costuma dar melhor resultado): ");
        Integer ano = lerInteiro("Ano de lançamento (opcional): ");
        Filme encontrado = service.consultarNoOmdb(titulo, ano);
        saida.println("Encontrado no OMDb (ainda não salvo):");
        imprimirFilme(encontrado);
    }

    private void importarDoOmdb() {
        String titulo = lerLinha("Título do filme (em inglês costuma dar melhor resultado): ");
        Integer ano = lerInteiro("Ano de lançamento (opcional): ");
        Filme salvo = service.importar(titulo, ano);
        saida.println("Filme importado com sucesso!");
        imprimirFilme(salvo);
    }

    // ------------------------------------------------------------------ entrada e saída

    private String lerLinha(String mensagem) {
        saida.print(mensagem);
        saida.flush();
        return entrada.hasNextLine() ? entrada.nextLine() : null;
    }

    /** Lê um inteiro opcional: linha vazia → null; texto não numérico → erro de dados inválidos. */
    private Integer lerInteiro(String mensagem) {
        String texto = lerLinha(mensagem);
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(texto.trim());
        } catch (NumberFormatException e) {
            throw new DadosInvalidosException("Valor numérico inválido: '" + texto.trim() + "'.");
        }
    }

    private Long lerId(String mensagem) {
        String texto = lerLinha(mensagem);
        if (texto == null || texto.isBlank()) {
            throw new DadosInvalidosException("O ID é obrigatório.");
        }
        try {
            return Long.valueOf(texto.trim());
        } catch (NumberFormatException e) {
            throw new DadosInvalidosException("ID inválido: '" + texto.trim() + "'.");
        }
    }

    private String lerOuManter(String rotulo, String atual) {
        String texto = lerLinha(rotulo + " [" + atual + "]: ");
        return (texto == null || texto.isBlank()) ? atual : texto;
    }

    private Integer lerInteiroOuManter(String rotulo, Integer atual) {
        Integer novo = lerInteiro(rotulo + " [" + atual + "]: ");
        return novo == null ? atual : novo;
    }

    private void imprimirFilme(Filme f) {
        saida.println("  ID......: " + valor(f.getId()));
        saida.println("  Título..: " + valor(f.getTitulo()));
        saida.println("  Gênero..: " + valor(f.getGenero()));
        saida.println("  Ano.....: " + valor(f.getAnoLancamento()));
        saida.println("  Duração.: " + duracao(f));
    }

    private static String valor(Object objeto) {
        return objeto == null ? NAO_INFORMADO : objeto.toString();
    }

    private static String duracao(Filme f) {
        return f.getDuracao() == null ? NAO_INFORMADO : f.getDuracao() + " min";
    }
}
