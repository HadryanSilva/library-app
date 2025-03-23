package br.com.hadryan.app.config;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe responsável por gerenciar a configuração JPA e fornecer acesso centralizado
 * aos EntityManagers. Implementa o padrão Singleton.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class JpaConfig {

    private static final Logger LOGGER = Logger.getLogger(JpaConfig.class.getName());
    private static final String PERSISTENCE_UNIT = "default";

    private static JpaConfig instance;
    private final EntityManagerFactory entityManagerFactory;

    // ThreadLocal para garantir que cada thread tenha seu próprio EntityManager
    private final ThreadLocal<EntityManager> threadLocalEntityManager = new ThreadLocal<>();

    /**
     * Construtor privado - padrão Singleton
     */
    private JpaConfig() {
        try {
            entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
            LOGGER.info("EntityManagerFactory inicializado com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao inicializar EntityManagerFactory", e);
            throw new RuntimeException("Erro ao inicializar banco de dados", e);
        }
    }

    /**
     * Retorna a instância única da configuração JPA
     */
    public static synchronized JpaConfig getInstance() {
        if (instance == null) {
            instance = new JpaConfig();
        }
        return instance;
    }

    /**
     * Obtém o EntityManager associado à thread atual ou cria um novo se necessário
     */
    public EntityManager getEntityManager() {
        EntityManager em = threadLocalEntityManager.get();

        if (em == null || !em.isOpen()) {
            em = entityManagerFactory.createEntityManager();
            threadLocalEntityManager.set(em);
        }

        return em;
    }

    /**
     * Fecha o EntityManager da thread atual
     */
    public void closeEntityManager() {
        EntityManager em = threadLocalEntityManager.get();
        if (em != null && em.isOpen()) {
            try {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                    LOGGER.warning("Transação ativa encontrada ao fechar EntityManager. Realizando rollback.");
                }
                em.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Erro ao fechar EntityManager", e);
            } finally {
                threadLocalEntityManager.remove();
            }
        }
    }

    /**
     * Inicia uma transação no EntityManager da thread atual
     */
    public void beginTransaction() {
        EntityManager em = getEntityManager();
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
    }

    /**
     * Commit da transação atual
     */
    public void commitTransaction() {
        EntityManager em = threadLocalEntityManager.get();
        if (em != null && em.getTransaction().isActive()) {
            em.getTransaction().commit();
        }
    }

    /**
     * Rollback da transação atual
     */
    public void rollbackTransaction() {
        EntityManager em = threadLocalEntityManager.get();
        if (em != null && em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }

    /**
     * Fecha todos os recursos
     */
    public void close() {
        closeEntityManager();

        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            try {
                entityManagerFactory.close();
                LOGGER.info("EntityManagerFactory fechado com sucesso");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erro ao fechar EntityManagerFactory", e);
            }
        }
    }

    /**
     * Verifica a conexão com o banco de dados
     */
    public boolean testarConexao() {
        try {
            EntityManager em = getEntityManager();
            em.createNativeQuery("SELECT 1").getSingleResult();
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao testar conexão com banco de dados", e);
            return false;
        }
    }
}