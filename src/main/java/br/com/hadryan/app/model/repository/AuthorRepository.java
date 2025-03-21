package br.com.hadryan.app.model.repository;

import br.com.hadryan.app.model.entity.Author;

import javax.persistence.TypedQuery;
import java.util.Optional;

/**
 * Repository para operações de CRUD da entidade Autor.
 * Implementa o padrão de projeto Repository.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class AuthorRepository extends GenericRepository<Author, Long> {

    /**
     * Busca um autor pelo nome
     *
     * @param name Nome do autor a ser buscado
     * @return Optional contendo o autor, se encontrado
     */
    public Optional<Author> findByName(String name) {
        TypedQuery<Author> query = entityManager.createQuery(
                "SELECT a FROM Author a WHERE LOWER(a.name) = LOWER(:name)", Author.class);
        query.setParameter("name", name);
        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    protected boolean isNewEntity(Author entity) {
        return entity.getId() == null;
    }
}
