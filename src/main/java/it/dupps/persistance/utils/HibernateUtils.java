package it.dupps.persistance.utils;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

/**
 * Created by dupps on 02.03.15.
 */
public enum HibernateUtils {
    INSTANCE;

    private SessionFactory sessionFactory;

    HibernateUtils() {
        sessionFactory = initializeSessionFactory();
    }

    private SessionFactory initializeSessionFactory() {
        if (sessionFactory == null) {
            Configuration config = new Configuration();
            config.addAnnotatedClass(it.dupps.persistance.data.Message.class);
            config.addAnnotatedClass(it.dupps.persistance.data.User.class);
            config.configure();
            StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
                    .applySettings(config.getProperties());
            sessionFactory = config.buildSessionFactory(builder.build());
        }
        return sessionFactory;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}