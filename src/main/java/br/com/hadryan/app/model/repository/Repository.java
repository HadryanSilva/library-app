package br.com.hadryan.app.model.repository;

import br.com.hadryan.app.config.JpaConfig;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Interface genérica para implementação do padrão Repository.
 * Define operações básicas de CRUD.
 *
 * @param <T> Tipo da entidade
 * @param <ID> Tipo do identificador da entidade
 */
public interface Repository<T, ID extends Serializable> {

    /**
     * Obtém a classe da entidade
     */
    Class<T> getEntityClass();

    /**
     * Obtém o EntityManager atual
     */
    default EntityManager getEntityManager() {
        return JpaConfig.getInstance().getEntityManager();
    }

    /**
     * Salva ou atualiza uma entidade
     */
    default T save(T entity) {
        EntityManager em = getEntityManager();
        try {
            JpaConfig.getInstance().beginTransaction();
            T savedEntity = em.merge(entity);
            JpaConfig.getInstance().commitTransaction();
            return savedEntity;
        } catch (Exception e) {
            JpaConfig.getInstance().rollbackTransaction();
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Erro ao salvar entidade", e);
            throw new RuntimeException("Erro ao salvar: " + e.getMessage(), e);
        }
    }

    /**
     * Busca uma entidade pelo ID
     */
    default Optional<T> findById(ID id) {
        EntityManager em = getEntityManager();
        T entity = em.find(getEntityClass(), id);
        return Optional.ofNullable(entity);
    }

    /**
     * Lista todas as entidades
     */
    default List<T> findAll() {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(getEntityClass());
        Root<T> root = cq.from(getEntityClass());
        cq.select(root);
        return em.createQuery(cq).getResultList();
    }

    /**
     * Exclui uma entidade
     */
    default void delete(ID id) {
        EntityManager em = getEntityManager();
        try {
            JpaConfig.getInstance().beginTransaction();
            T entity = em.find(getEntityClass(), id);
            if (entity != null) {
                em.remove(entity);
            }
            JpaConfig.getInstance().commitTransaction();
        } catch (Exception e) {
            JpaConfig.getInstance().rollbackTransaction();
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Erro ao excluir entidade", e);
            throw new RuntimeException("Erro ao excluir: " + e.getMessage(), e);
        }
    }

    /**
     * Retorna o número de entidades
     */
    default long count() {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        cq.select(cb.count(cq.from(getEntityClass())));
        return em.createQuery(cq).getSingleResult();
    }
}
