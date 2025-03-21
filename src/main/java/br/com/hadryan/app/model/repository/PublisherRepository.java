package br.com.hadryan.app.model.repository;

import br.com.hadryan.app.model.entity.Publisher;
import br.com.hadryan.app.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

/**
 * Repository para operações de CRUD da entidade Editora.
 * Implementa o padrão de projeto DAO (Data Access Object).
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class PublisherRepository {

    private final EntityManager entityManager;

    public PublisherRepository() {
        entityManager = JPAUtil.getEntityManager();
    }

    /**
     * Salva uma editora no banco de dados
     *
     * @param publisher Objeto Publisher a ser salvo
     * @return Publisher com ID gerado
     */
    public Publisher save(Publisher publisher) {
        try {
            entityManager.getTransaction().begin();
            if (publisher.getId() == null) {
                entityManager.persist(publisher);
            } else {
                publisher = entityManager.merge(publisher);
            }
            entityManager.getTransaction().commit();
            return publisher;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    /**
     * Busca uma editora pelo ID
     *
     * @param id ID da editora a ser buscada
     * @return Optional contendo a editora, se encontrada
     */
    public Optional<Publisher> findById(Long id) {
        Publisher publisher = entityManager.find(Publisher.class, id);
        return Optional.ofNullable(publisher);
    }

    /**
     * Busca uma Editora pelo nome
     *
     * @param name Nome da Publisher a ser buscado
     * @return Optional contendo a Publisher, se encontrado
     */
    public Optional<Publisher> findByName(String name) {
        TypedQuery<Publisher> query = entityManager.createQuery(
                "SELECT p FROM Publisher p WHERE p.name = :name", Publisher.class);
        query.setParameter("name", name);
        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Lista todos as editoras
     *
     * @return Lista de todos as publishers
     */
    public List<Publisher> findAll() {
        TypedQuery<Publisher> query = entityManager.createQuery("SELECT p FROM Publisher p", Publisher.class);
        return query.getResultList();
    }

    /**
     * Exclui uma publisher pelo ID
     *
     * @param id ID da publisher a ser excluído
     */
    public void delete(Long id) {
        try {
            entityManager.getTransaction().begin();
            Publisher publisher = entityManager.find(Publisher.class, id);
            if (publisher != null) {
                entityManager.remove(publisher);
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
