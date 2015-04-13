package it.dupps.utils;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

/**
 * Created by dupps on 02.03.15.
 */
public enum HibernateUtils {

    INSTANCE;
    public static SessionFactory sessionFactory = null;

    private synchronized SessionFactory initialiseSessionFactory() {
        if (sessionFactory == null) {
            Configuration config = new Configuration();
            config.addAnnotatedClass(it.dupps.data.Message.class);
            config.addAnnotatedClass(it.dupps.data.User.class);
            config.configure();
            StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
                    .applySettings(config.getProperties());
            sessionFactory = config.buildSessionFactory(builder.build());
        }
        return sessionFactory;
    }

    public SessionFactory getSessionFactory() {
        SessionFactory factory = (sessionFactory == null) ?
                initialiseSessionFactory() : sessionFactory;
        return factory;
    }
}