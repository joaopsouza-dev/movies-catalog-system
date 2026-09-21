package br.com.catalogofilmes.cli;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import br.com.catalogofilmes.service.FilmeService;

/**
 * Inicia a CLI quando a propriedade app.cli.ativo=true (perfil "cli").
 * Nos testes e na execução normal da API, este componente nem é criado.
 */
@Component
@ConditionalOnProperty(name = "app.cli.ativo", havingValue = "true")
public class CliRunner implements ApplicationRunner {

    private final FilmeService service;
    private final ApplicationContext contexto;

    public CliRunner(FilmeService service, ApplicationContext contexto) {
        this.service = service;
        this.contexto = contexto;
    }

    @Override
    public void run(ApplicationArguments args) {
        new MenuCli(service, System.in, System.out).executar();
        // Encerra a aplicação depois que o usuário sai do menu.
        System.exit(SpringApplication.exit(contexto));
    }
}
