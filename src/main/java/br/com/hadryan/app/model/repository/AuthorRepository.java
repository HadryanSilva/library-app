package br.com.hadryan.app.model.repository;

import br.com.hadryan.app.model.entity.Author;
import br.com.hadryan.app.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

/**
 * Repository para operações de CRUD da entidade Autor.
 * Implementa o padrão de projeto DAO (Data Access Object).
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class AuthorRepository {

    private final EntityManager entityManager;

    public AuthorRepository() {
        entityManager = JPAUtil.getEntityManager();
    }

    /**
     * Salva um Autor no banco de dados
     *
     * @param author Objeto Author a ser salvo
     * @return Author com ID gerado
     */
    public Author save(Author author) {
        try {
            entityManager.getTransaction().begin();
            if (author.getId() == null) {
                entityManager.persist(author);
            } else {
                author = entityManager.merge(author);
            }
            entityManager.getTransaction().commit();
            return author;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    /**
     * Busca um Autor pelo ID
     *
     * @param id ID do autor a ser buscado
     * @return Optional contendo o autor, se encontrado
     */
    public Optional<Author> findById(Long id) {
        Author author = entityManager.find(Author.class, id);
        return Optional.ofNullable(author);
    }

    /**
     * Busca um autor pelo nome
     *
     * @param name Nome do autor a ser buscado
     * @return Optional contendo o autor, se encontrado
     */
    public Optional<Author> findByName(String name) {
        TypedQuery<Author> query = entityManager.createQuery(
                "SELECT a FROM Author a WHERE a.name = :name", Author.class);
        query.setParameter("name", name);
        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Lista todos os autores
     *
     * @return Lista de todos os autores
     */
    public List<Author> findAll() {
        TypedQuery<Author> query = entityManager.createQuery("SELECT a FROM Author a", Author.class);
        return query.getResultList();
    }

    /**
     * Exclui um autor pelo ID
     *
     * @param id ID do autor a ser excluído
     */
    public void delete(Long id) {
        try {
            entityManager.getTransaction().begin();
            Author author = entityManager.find(Author.class, id);
            if (author != null) {
                entityManager.remove(author);
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    /**
     * Fecha o EntityManager
     */
    public void close() {
        if (entityManager.isOpen()) {
            entityManager.close();
        }
    }

}
