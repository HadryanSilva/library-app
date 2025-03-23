package br.com.hadryan.app.model.repository;

import br.com.hadryan.app.model.entity.Autor;
import br.com.hadryan.app.model.entity.Livro;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Repositório para operações de CRUD de Livros.
 *
 * @author Hadryan Silva
 * @since 23-03-2025
 */
public class LivroRepository implements Repository<Livro, Long> {

    private static final Logger LOGGER = Logger.getLogger(LivroRepository.class.getName());

    @Override
    public Class<Livro> getEntityClass() {
        return Livro.class;
    }

    /**
     * Busca um livro pelo ISBN
     */
    public Optional<Livro> findByIsbn(String isbn) {
        try {
            TypedQuery<Livro> query = getEntityManager().createQuery(
                    "SELECT l FROM Livro l WHERE l.isbn = :isbn", Livro.class);
            query.setParameter("isbn", isbn);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao buscar livro por ISBN: " + isbn, e);
            return Optional.empty();
        }
    }

    /**
     * Busca livros com base em critérios de pesquisa
     */
    public List<Livro> search(Livro filtro) {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Livro> cq = cb.createQuery(Livro.class);
            Root<Livro> root = cq.from(Livro.class);

            List<Predicate> predicates = new ArrayList<>();

            if (filtro.getTitulo() != null && !filtro.getTitulo().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("titulo")),
                        "%" + filtro.getTitulo().toLowerCase() + "%"
                ));
            }

            if (filtro.getIsbn() != null && !filtro.getIsbn().isEmpty()) {
                predicates.add(cb.like(
                        root.get("isbn"),
                        "%" + filtro.getIsbn() + "%"
                ));
            }

            if (filtro.getDataPublicacao() != null && !filtro.getDataPublicacao().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("dataPublicacao")),
                        "%" + filtro.getDataPublicacao().toLowerCase() + "%"
                ));
            }

            if (filtro.getAutores() != null && !filtro.getAutores().isEmpty()) {
                String nomeAutor = filtro.getAutores().iterator().next().getNome().toLowerCase();
                Join<Livro, Autor> autorJoin = root.join("autores");
                predicates.add(cb.like(
                        cb.lower(autorJoin.get("nome")),
                        "%" + nomeAutor + "%"
                ));
            }

            if (filtro.getEditora() != null && filtro.getEditora().getNome() != null) {
                predicates.add(cb.like(
                        cb.lower(root.get("editora").get("nome")),
                        "%" + filtro.getEditora().getNome().toLowerCase() + "%"
                ));
            }

            if (!predicates.isEmpty()) {
                cq.where(cb.and(predicates.toArray(new Predicate[0])));
            }

            return getEntityManager().createQuery(cq).getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao pesquisar livros", e);
            return new ArrayList<>();
        }
    }
}