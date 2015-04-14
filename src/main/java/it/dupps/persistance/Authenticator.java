package it.dupps.persistance;

import it.dupps.persistance.data.User;
import it.dupps.persistance.utils.HibernateUtils;
import org.hibernate.*;

import java.util.List;
import java.util.UUID;

/**
 * Created by dupps on 14.04.15.
 */
public class Authenticator {

    private static SessionFactory sessionFactory;

    public UUID authenticate(String username, String password) {
        sessionFactory = HibernateUtils.INSTANCE.getSessionFactory();
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        List<User> users = null;
        try {
            Query query = session.createQuery(new StringBuilder()
                    .append("FROM User WHERE")
                    .append("(email = :email) and (password = :password)")
                    .toString());
            query.setParameter("email", username);
            query.setParameter("password", password);
            users = (List<User>) query.list();
            if (tx != null) tx.commit();

        } catch (HibernateException he) {
            if (tx != null) tx.rollback();
            he.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        if(users != null && users.size() > 0)
            return UUID.randomUUID();

        return null;
    }
}
