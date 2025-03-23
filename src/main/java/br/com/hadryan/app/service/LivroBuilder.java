package br.com.hadryan.app.service;

import br.com.hadryan.app.model.entity.Autor;
import br.com.hadryan.app.model.entity.Editora;
import br.com.hadryan.app.model.entity.Livro;

import java.util.HashSet;
import java.util.List;

/**
 * Builder para criar objetos Livro de forma fluente
 *
 * @author Hadryan Silva
 * @since 23-03-2025
 */
public class LivroBuilder {
    private final Livro livro;

    /**
     * Construtor padrão que cria um novo livro vazio
     */
    public LivroBuilder() {
        this.livro = new Livro();
    }

    /**
     * Define o ISBN do livro
     */
    public LivroBuilder comIsbn(String isbn) {
        livro.setIsbn(isbn);
        return this;
    }

    /**
     * Define o título do livro
     */
    public LivroBuilder comTitulo(String titulo) {
        livro.setTitulo(titulo);
        return this;
    }

    /**
     * Define a data de publicação do livro
     */
    public LivroBuilder comDataPublicacao(String dataPublicacao) {
        livro.setDataPublicacao(dataPublicacao);
        return this;
    }

    /**
     * Define a editora do livro
     */
    public LivroBuilder comEditora(Editora editora) {
        livro.setEditora(editora);
        return this;
    }

    /**
     * Define a editora do livro pelo nome
     */
    public LivroBuilder comEditora(String nomeEditora) {
        if (nomeEditora != null && !nomeEditora.trim().isEmpty()) {
            livro.setEditora(new Editora(nomeEditora));
        }
        return this;
    }

    /**
     * Adiciona um autor ao livro
     */
    public LivroBuilder comAutor(Autor autor) {
        if (autor != null) {
            livro.adicionarAutor(autor);
        }
        return this;
    }

    /**
     * Adiciona um autor ao livro pelo nome
     */
    public LivroBuilder comAutor(String nomeAutor) {
        if (nomeAutor != null && !nomeAutor.trim().isEmpty()) {
            livro.adicionarAutor(new Autor(nomeAutor));
        }
        return this;
    }

    /**
     * Define a lista completa de autores do livro
     */
    public LivroBuilder comAutores(List<Autor> autores) {
        if (autores != null) {
            livro.setAutores(new HashSet<>(autores));
        }
        return this;
    }

    /**
     * Constrói o objeto Livro
     */
    public Livro build() {
        return livro;
    }
}