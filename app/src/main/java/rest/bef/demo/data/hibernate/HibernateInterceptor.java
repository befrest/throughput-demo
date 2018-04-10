package rest.bef.demo.data.hibernate;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import rest.bef.demo.model.entity.BaseEntity;

import java.io.Serializable;
import java.util.Date;

public class HibernateInterceptor extends EmptyInterceptor {

    private static final long serialVersionUID = 3096509331974837890L;

    public boolean onSave(
            Object entity,
            Serializable id,
            Object[] state,
            String[] propertyNames,
            Type[] types) {

        if (entity instanceof BaseEntity) {
            setModifiedValues(state, propertyNames, false);
            return true;
        }

        return false;
    }

    public boolean onFlushDirty(
            Object entity,
            Serializable id,
            Object[] currentState,
            Object[] previousState,
            String[] propertyNames,
            Type[] types) {

        if (entity instanceof BaseEntity) {
            setModifiedValues(currentState, propertyNames, true);
            return true;
        }

        return false;
    }

    private void setModifiedValues(Object[] state, String[] propertyNames, boolean updated) {
        boolean modifiedCreated = false;
        boolean modifiedUpdated = !updated;

        for (int i = 0; i < propertyNames.length; i++) {
            if (!updated && "created".equals(propertyNames[i])) {
                state[i] = new Date();
                modifiedCreated = true;
                continue;
            }

            if (updated && "updated".equals(propertyNames[i])) {
                state[i] = new Date();
                modifiedUpdated = true;
                continue;
            }

            //noinspection ConstantConditions
            if (modifiedCreated && modifiedUpdated)
                break;
        }
    }

}
