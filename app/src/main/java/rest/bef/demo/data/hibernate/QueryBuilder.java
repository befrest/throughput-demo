package rest.bef.demo.data.hibernate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.Map;

public class QueryBuilder {

    private static final Logger LOGGER = LogManager.getLogger();
    private String query;
    private Map<String, Object> namedParams;
    private Integer firstResult;
    private Integer maxResults;

    public QueryBuilder(String query) {
        this.query = query;
    }

    public <T> List<T> list() {
        Query query = buildQuery();

        if (getFirstResult() != null)
            query = query.setFirstResult(getFirstResult());

        if (getMaxResults() != null)
            query = query.setMaxResults(getMaxResults());

        //noinspection unchecked
        return query.list();
    }

    public <T> List<T> list(Object... params) {
        Query query = buildQuery(params);

        if (getFirstResult() != null)
            query = query.setFirstResult(getFirstResult());

        if (getMaxResults() != null)
            query = query.setMaxResults(getMaxResults());

        //noinspection unchecked
        return query.list();
    }

    public <T> List<T> listSQL(Object... params) {
        Query query = buildNativeQuery(params);

        if (getFirstResult() != null)
            query = query.setFirstResult(getFirstResult());

        if (getMaxResults() != null)
            query = query.setMaxResults(getMaxResults());

        //noinspection unchecked
        return (List<T>) query.list();
    }

    public <T> T object() {
        try {
            //noinspection unchecked
            return (T) buildQuery().uniqueResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    public <T> T objectSQL(Object... params) {
        Query query = buildNativeQuery(params);

        if (getFirstResult() != null)
            query = query.setFirstResult(getFirstResult());

        if (getMaxResults() != null)
            query = query.setMaxResults(getMaxResults());

        //noinspection unchecked
        return (T) query.uniqueResult();
    }

    public <T> T object(Object... params) {
        try {
            //noinspection unchecked
            return (T) buildQuery(params).uniqueResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    public int execute() {
        try {
            HibernateUtil.beginTransaction();
            int affected = buildQuery().executeUpdate();
            HibernateUtil.commitTransaction();
            return affected;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            HibernateUtil.rollbackTransaction();
            return 0;
        }
    }

    public int execute(Object... params) {
        try {
            HibernateUtil.beginTransaction();
            int affected = buildQuery(params).executeUpdate();
            HibernateUtil.commitTransaction();
            return affected;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            HibernateUtil.rollbackTransaction();
            return 0;
        }
    }

    public int executeSQL(Object... params) {
        try {
            HibernateUtil.beginTransaction();
            int affected = buildNativeQuery(params).executeUpdate();
            HibernateUtil.commitTransaction();
            return affected;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            HibernateUtil.rollbackTransaction();
            return 0;
        }
    }

    public Map<String, Object> getNamedParams() {
        return namedParams;
    }

    public QueryBuilder setNamedParams(Map<String, Object> namedParams) {
        this.namedParams = namedParams;
        return this;
    }

    public Integer getFirstResult() {
        return firstResult;
    }

    public QueryBuilder setFirstResult(Integer firstResult) {
        this.firstResult = firstResult;
        return this;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public QueryBuilder setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    private Query buildQuery() {
        Query myQuery = HibernateUtil.getSession().createQuery(query);

        if (namedParams != null && namedParams.size() > 0)
            for (Map.Entry<String, Object> entry : namedParams.entrySet())
                myQuery.setParameter(entry.getKey(), entry.getValue());

        return myQuery;
    }

    private Query buildQuery(Object... params) {
        Query myQuery = HibernateUtil.getSession().createQuery(query);

        if (params != null && params.length > 0)
            for (int i = 0; i < params.length; i++)
                myQuery.setParameter(String.valueOf(i + 1), params[i]);

        return myQuery;
    }

    private Query buildNativeQuery(Object... params) {
        Query myQuery = HibernateUtil.getSession().createSQLQuery(query);

        if (params != null && params.length > 0)
            for (int i = 0; i < params.length; i++)
                myQuery.setParameter(i, params[i]);

        return myQuery;
    }
}