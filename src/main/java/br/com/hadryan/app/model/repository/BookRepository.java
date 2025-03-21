package br.com.hadryan.app.model.repository;

import br.com.hadryan.app.model.entity.Author;
import br.com.hadryan.app.model.entity.Book;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository para operações de CRUD da entidade Livro.
 * Implementa o padrão de projeto DAO (Data Access Object).
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class BookRepository extends GenericRepository<Book, Long> {

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
     * Busca livros que correspondam aos critérios de pesquisa
     *
     * @param filter Objeto Livro com os critérios de pesquisa
     * @return Lista de livros que correspondem aos critérios
     */
    public List<Book> search(Book filter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Book> cq = cb.createQuery(Book.class);
        Root<Book> root = cq.from(Book.class);

        List<Predicate> predicates = createPredicates(filter, cb, root);

        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        TypedQuery<Book> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    /**
     * Método auxiliar para criar predicados para a pesquisa
     */
    private List<Predicate> createPredicates(Book filter, CriteriaBuilder cb, Root<Book> root) {
        List<Predicate> predicates = new ArrayList<>();

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
        if (filter.getAuthors() != null && !filter.getAuthors().isEmpty()) {
            String authorName = filter.getAuthors().iterator().next().getName().toLowerCase();
            javax.persistence.criteria.Join<Book, Author> authorJoin = root.join("authors");
            predicates.add(cb.like(cb.lower(authorJoin.get("name")), "%" + authorName + "%"));
        }

        if (filter.getPublisher() != null && filter.getPublisher().getName() != null) {
            predicates.add(cb.like(
                    cb.lower(root.get("publisher").get("name")),
                    "%" + filter.getPublisher().getName().toLowerCase() + "%"
            ));
        }

        return predicates;
    }

    @Override
    protected boolean isNewEntity(Book entity) {
        return entity.getId() == null;
    }
}
