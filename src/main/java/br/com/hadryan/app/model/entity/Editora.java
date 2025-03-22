package br.com.hadryan.app.model.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidade que representa uma editora no sistema.
 */
@Entity
@Table(name = "editora")
public class Editora implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @OneToMany(mappedBy = "editora")
    private Set<Livro> livros = new HashSet<>();

    /**
     * Construtor padrão
     */
    public Editora() {
    }

    /**
     * Construtor com nome
     */
    public Editora(String nome) {
        this.nome = nome;
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Set<Livro> getLivros() {
        return livros;
    }

    public void setLivros(Set<Livro> livros) {
        this.livros = livros;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Editora editora = (Editora) o;
        return Objects.equals(nome, editora.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome);
    }

    @Override
    public String toString() {
        return nome;
    }
}