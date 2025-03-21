package br.com.hadryan.app.model.repository;

import br.com.hadryan.app.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class GenericRepository<T, ID extends Serializable> {

    private static final Logger LOGGER = Logger.getLogger(GenericRepository.class.getName());

    protected final EntityManager entityManager;
    protected final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    public GenericRepository() {
        this.entityManager = JPAUtil.getEntityManager();
        this.entityClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * Salva uma entidade no banco de dados
     *
     * @param entity Entidade a ser salva
     * @return Entidade com ID gerado
     */
    public T save(T entity) {
        try {
            entityManager.getTransaction().begin();

            boolean isNew = isNewEntity(entity);

            if (isNew) {
                entityManager.persist(entity);
            } else {
                entity = entityManager.merge(entity);
            }

            entityManager.getTransaction().commit();
            return entity;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            LOGGER.log(Level.SEVERE, "Erro ao salvar entidade: " + e.getMessage(), e);
            throw new RuntimeException("Erro ao salvar: " + e.getMessage(), e);
        }
    }

    /**
     * Busca uma entidade pelo ID
     *
     * @param id ID da entidade a ser buscada
     * @return Optional contendo a entidade, se encontrada
     */
    public Optional<T> findById(ID id) {
        T entity = entityManager.find(entityClass, id);
        return Optional.ofNullable(entity);
    }

    /**
     * Lista todas as entidades
     *
     * @return Lista de todas as entidades
     */
    public List<T> findAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);
        cq.select(root);

        TypedQuery<T> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    /**
     * Exclui uma entidade pelo ID
     *
     * @param id ID da entidade a ser excluída
     */
    public void delete(ID id) {
        try {
            entityManager.getTransaction().begin();
            T entity = entityManager.find(entityClass, id);
            if (entity != null) {
                entityManager.remove(entity);
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            LOGGER.log(Level.SEVERE, "Erro ao excluir entidade: " + e.getMessage(), e);
            throw new RuntimeException("Erro ao excluir: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica se a entidade é nova (não tem ID)
     *
     * @param entity Entidade a ser verificada
     * @return true se a entidade for nova, false caso contrário
     */
    protected abstract boolean isNewEntity(T entity);

    /**
     * Fecha o EntityManager
     */
    public void close() {
        if (entityManager.isOpen()) {
            entityManager.close();
        }
    }

}
