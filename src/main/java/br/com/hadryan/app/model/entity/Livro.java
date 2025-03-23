package br.com.hadryan.app.model.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidade que representa um livro no sistema.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
@Entity
@Table(name = "livro")
public class Livro implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "isbn", unique = true, length = 20)
    private String isbn;

    @Column(name = "data_publicacao", length = 50)
    private String dataPublicacao;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "editora_id")
    private Editora editora;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "livro_autor",
            joinColumns = @JoinColumn(name = "livro_id"),
            inverseJoinColumns = @JoinColumn(name = "autor_id")
    )
    private Set<Autor> autores = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "livros_similares",
            joinColumns = @JoinColumn(name = "livro_id"),
            inverseJoinColumns = @JoinColumn(name = "similar_id")
    )
    private Set<Livro> livrosSimilares = new HashSet<>();

    /**
     * Construtor padrão
     */
    public Livro() {
    }

    /**
     * Construtor com parâmetros básicos
     */
    public Livro(String titulo, String isbn) {
        this.titulo = titulo;
        this.isbn = isbn;
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getDataPublicacao() {
        return dataPublicacao;
    }

    public void setDataPublicacao(String dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }

    public Editora getEditora() {
        return editora;
    }

    public void setEditora(Editora editora) {
        this.editora = editora;
    }

    public Set<Autor> getAutores() {
        return autores;
    }

    public void setAutores(Set<Autor> autores) {
        this.autores = autores;
    }

    /**
     * Adiciona um autor ao livro
     */
    public void adicionarAutor(Autor autor) {
        this.autores.add(autor);
        autor.getLivros().add(this);
    }

    /**
     * Remove um autor do livro
     */
    public void removerAutor(Autor autor) {
        this.autores.remove(autor);
        autor.getLivros().remove(this);
    }

    public Set<Livro> getLivrosSimilares() {
        return livrosSimilares;
    }

    public void setLivrosSimilares(Set<Livro> livrosSimilares) {
        this.livrosSimilares = livrosSimilares;
    }

    /**
     * Adiciona um livro similar
     */
    public void adicionarLivroSimilar(Livro livro) {
        this.livrosSimilares.add(livro);
    }

    /**
     * Remove um livro similar
     */
    public void removerLivroSimilar(Livro livro) {
        this.livrosSimilares.remove(livro);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Livro livro = (Livro) o;
        return Objects.equals(isbn, livro.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }

    @Override
    public String toString() {
        return "Livro{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", isbn='" + isbn + '\'' +
                '}';
    }
}