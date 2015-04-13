package it.dupps.server;

import it.dupps.persistance.data.Message;
import it.dupps.persistance.data.User;
import it.dupps.persistance.utils.HibernateUtils;
import org.hibernate.*;

import java.util.List;
import java.util.UUID;

/**
 * Created by dupps on 13.04.15.
 */
public class CSPersistance {

    private static SessionFactory sessionFactory;

    public List<Message> getHistoryMessages(Integer amount) {
        sessionFactory = HibernateUtils.INSTANCE.getSessionFactory();
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        List<Message> messages = null;
        try {
            Query query = session.createQuery(
                    "FROM Message ORDER BY messageTimestamp DESC");
            query.setFirstResult(0);
            query.setMaxResults(amount);
            messages = (List<Message>) query.list();
            if (tx != null) tx.commit();

        } catch (HibernateException he) {
            if (tx != null) tx.rollback();
            he.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return messages;
    }

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
