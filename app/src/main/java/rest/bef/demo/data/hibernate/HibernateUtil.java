package rest.bef.demo.data.hibernate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;


public class HibernateUtil {

    private static Logger logger = LogManager.getLogger();
    private static SessionFactory sessionFactory;
    private static final ThreadLocal<Session> session = new ThreadLocal<>();
/*
    static {
        try {
            Configuration cfg = new Configuration();
//            cfg
//                    .addAnnotatedClass(UserEntity.class)
//                    .addAnnotatedClass(OrderEntity.class);
            sessionFactory = cfg.configure().buildSessionFactory();

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
*/

    private static Session currentSession() throws HibernateException {
        Session s = session.get();

        if (s == null || !s.isOpen())
            s = sessionFactory
                    .withOptions()
                    .interceptor(new HibernateInterceptor())
                    .openSession();

        s.setHibernateFlushMode(FlushMode.COMMIT);
        session.set(s);
        return session.get();
    }

    public static Session getSession() throws HibernateException {
        return currentSession();
    }

    public static void beginTransaction() {
        getSession().beginTransaction();
    }

    public static void commitTransaction() {
        getSession().getTransaction().commit();
    }

    public static void rollbackTransaction() {
        getSession().getTransaction().rollback();
    }

    public static void save(Object toSave) {
        getSession().save(toSave);
    }

    public static void saveOrUpdate(Object toSave) {
        getSession().saveOrUpdate(toSave);
    }

    public static void update(Object toSave) {
        getSession().update(toSave);
    }

    public static void clearCache() {
        Session s = session.get();

        if (s != null && s.isOpen())
            s.clear();

        sessionFactory.getCache().evictQueryRegions();
    }

    public static void closeSession() throws HibernateException {
        Session s = session.get();

        if (s != null && s.isOpen()) {
            s.clear();
            s.close();
        }

        session.set(null);
    }
}
