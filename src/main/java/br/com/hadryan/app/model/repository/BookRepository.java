package br.com.hadryan.app.model.repository;

import br.com.hadryan.app.model.entity.Book;
import br.com.hadryan.app.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

/**
 * Repository para operações de CRUD da entidade Livro.
 * Implementa o padrão de projeto DAO (Data Access Object).
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class BookRepository {

    private final EntityManager entityManager;

    public BookRepository() {
        entityManager = JPAUtil.getEntityManager();
    }

    /**
     * Salva um livro no banco de dados
     *
     * @param book Objeto Book a ser salvo
     * @return Book com ID gerado
     */
    public Book save(Book book) {
        try {
            entityManager.getTransaction().begin();
            if (book.getId() == null) {
                entityManager.persist(book);
            } else {
                book = entityManager.merge(book);
            }
            entityManager.getTransaction().commit();
            return book;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    /**
     * Busca um livro pelo ID
     *
     * @param id ID do livro a ser buscado
     * @return Optional contendo o livro, se encontrado
     */
    public Optional<Book> findById(Long id) {
        Book book = entityManager.find(Book.class, id);
        return Optional.ofNullable(book);
    }

    /**
     * Busca um livro pelo ISBN
     *
     * @param isbn ISBN do livro a ser buscado
     * @return Optional contendo o livro, se encontrado
     */
    public Optional<Book> findByIsbn(String isbn) {
        TypedQuery<Book> query = entityManager.createQuery(
                "SELECT b FROM Book b WHERE b.isbn = :isbn", Book.class);
        query.setParameter("isbn", isbn);
        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Lista todos os livros
     *
     * @return Lista de todos os livros
     */
    public List<Book> findAll() {
        TypedQuery<Book> query = entityManager.createQuery("SELECT b FROM Book b", Book.class);
        return query.getResultList();
    }

    /**
     * Exclui um livro pelo ID
     *
     * @param id ID do livro a ser excluído
     */
    public void delete(Long id) {
        try {
            entityManager.getTransaction().begin();
            Book book = entityManager.find(Book.class, id);
            if (book != null) {
                entityManager.remove(book);
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    /**
     * Busca livros que correspondam aos critérios de pesquisa
     *
     * @param filter Objeto Livro com os critérios de pesquisa
     * @return Lista de livros que correspondem aos critérios
     */
    public List<Book> search(Book filter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Book> cq = cb.createQuery(Book.class);
        Root<Book> root = cq.from(Book.class);

        Predicate[] predicates = createPredicates(filter, cb, root);
        cq.where(predicates);

        TypedQuery<Book> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    /**
     * Método auxiliar para criar predicados para a pesquisa
     */
    private Predicate[] createPredicates(Book filter, CriteriaBuilder cb, Root<Book> root) {
        // List of predicates for the search
        List<Predicate> predicates = new java.util.ArrayList<>();

        if (filter.getTitle() != null && !filter.getTitle().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("title")),
                    "%" + filter.getTitle().toLowerCase() + "%"));
        }

        if (filter.getIsbn() != null && !filter.getIsbn().isEmpty()) {
            predicates.add(cb.like(root.get("isbn"), "%" + filter.getIsbn() + "%"));
        }

        if (filter.getPublicationDate() != null) {
            predicates.add(cb.equal(root.get("publicationDate"), filter.getPublicationDate()));
        }

        return predicates.toArray(new Predicate[0]);
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
