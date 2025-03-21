package br.com.hadryan.app.model.repository;

import br.com.hadryan.app.model.entity.Publisher;

import javax.persistence.TypedQuery;
import java.util.Optional;

/**
 * Repository para operações de CRUD da entidade Editora.
 * Implementa o padrão de projeto Repository.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class PublisherRepository extends GenericRepository<Publisher, Long> {

    /**
     * Busca uma Editora pelo nome
     *
     * @param name Nome da Publisher a ser buscado
     * @return Optional contendo a Publisher, se encontrado
     */
    public Optional<Publisher> findByName(String name) {
        TypedQuery<Publisher> query = entityManager.createQuery(
                "SELECT p FROM Publisher p WHERE LOWER(p.name) = LOWER(:name)", Publisher.class);
        query.setParameter("name", name);
        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    protected boolean isNewEntity(Publisher entity) {
        return entity.getId() == null;
    }
}
