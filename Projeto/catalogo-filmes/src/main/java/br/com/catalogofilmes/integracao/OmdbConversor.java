package br.com.catalogofilmes.integracao;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.catalogofilmes.model.Filme;

/**
 * Converte a resposta do OMDb (textos como "148 min" ou "2010–2015") para o modelo {@link Filme}.
 * Valores ausentes ("N/A") viram null; quem cadastra decide se isso é aceitável.
 */
public final class OmdbConversor {

    private static final Pattern ANO = Pattern.compile("^\\s*(\\d{4})");
    private static final Pattern MINUTOS = Pattern.compile("(\\d+)");

    private OmdbConversor() {
    }

    public static Filme paraFilme(OmdbResposta resposta) {
        return new Filme(
                limpar(resposta.titulo()),
                ajustarGenero(limpar(resposta.genero())),
                extrairAno(resposta.ano()),
                extrairMinutos(resposta.duracao()));
    }

    /** Remove espaços e transforma "N/A" e texto vazio em null. */
    static String limpar(String valor) {
        if (valor == null) {
            return null;
        }
        String texto = valor.trim();
        return (texto.isEmpty() || texto.equalsIgnoreCase("N/A")) ? null : texto;
    }

    /** "2010" → 2010; "2010–2015" → 2010; "N/A" → null. */
    static Integer extrairAno(String valor) {
        String texto = limpar(valor);
        if (texto == null) {
            return null;
        }
        Matcher m = ANO.matcher(texto);
        return m.find() ? Integer.valueOf(m.group(1)) : null;
    }

    /** "148 min" → 148; "N/A" → null. */
    static Integer extrairMinutos(String valor) {
        String texto = limpar(valor);
        if (texto == null) {
            return null;
        }
        Matcher m = MINUTOS.matcher(texto);
        if (!m.find()) {
            return null;
        }
        try {
            return Integer.valueOf(m.group(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * O OMDb devolve vários gêneros ("Action, Adventure, Sci-Fi"). Se a lista passar do limite do
     * campo (50 caracteres), mantém apenas os primeiros gêneros que cabem.
     */
    static String ajustarGenero(String genero) {
        if (genero == null || genero.length() <= Filme.GENERO_MAX) {
            return genero;
        }
        StringBuilder resultado = new StringBuilder();
        for (String parte : genero.split(",")) {
            String candidato = resultado.length() == 0
                    ? parte.trim()
                    : resultado + ", " + parte.trim();
            if (candidato.length() > Filme.GENERO_MAX) {
                break;
            }
            resultado.setLength(0);
            resultado.append(candidato);
        }
        return resultado.length() == 0 ? genero.substring(0, Filme.GENERO_MAX) : resultado.toString();
    }
}
