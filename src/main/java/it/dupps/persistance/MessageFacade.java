package it.dupps.persistance;

import it.dupps.persistance.data.Message;
import it.dupps.persistance.utils.HibernateUtils;
import org.hibernate.*;

import java.util.List;

/**
 * Created by dupps on 13.04.15.
 */
public class MessageFacade {

    private static SessionFactory sessionFactory;

    public int persistMessage(String text, String source) {
        sessionFactory = HibernateUtils.INSTANCE.getSessionFactory();
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        Integer messageId = null;

        try {
            tx = session.beginTransaction();
            Message messageObject = new Message();
            messageObject.setMessageText(text);
            messageObject.setMessageSource(source);
            messageId = (Integer) session.save(messageObject);
            tx.commit();

        } catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return messageId;
    }

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
}
