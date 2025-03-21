package br.com.hadryan.app.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Classe utilitária para gerenciar a conexão JPA e criar EntityManagers.
 * Implementa o padrão Singleton para o EntityManagerFactory.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class JPAUtil {

    private static EntityManagerFactory entityManagerFactory;

    private static void initialize() {
        try {
            if (entityManagerFactory == null) {
                entityManagerFactory = Persistence.createEntityManagerFactory("LibraryPU");
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error initializing EntityManagerFactory", ex);
        }
    }

    public static EntityManager getEntityManager() {
        if (entityManagerFactory == null) {
            initialize();
        }
        return entityManagerFactory.createEntityManager();
    }

    public static void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

}
