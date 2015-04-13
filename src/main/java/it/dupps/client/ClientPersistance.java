package it.dupps.client;

import it.dupps.data.Message;
import it.dupps.data.User;
import it.dupps.utils.HibernateUtils;
import org.hibernate.*;

import java.util.List;

/**
 * Created by dupps on 13.04.15.
 */
public class ClientPersistance {

    private static SessionFactory sessionFactory;

    protected List<Message> getHistoryMessages(Integer amount) {
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

    protected User getUser(String usr, String pwd) {
        sessionFactory = HibernateUtils.INSTANCE.getSessionFactory();
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        List<User> users = null;

        try {
            Query query = session.createQuery(new StringBuilder()
                    .append("FROM User WHERE")
                    .append("(email = :email) and (password = :password)")
                    .toString());
            query.setParameter("email",usr);
            query.setParameter("password",pwd);
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
            return users.get(0);

        return null;
    }
}
