package jmri;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Utility class for managing access to a NamedBean
 *
 * @author Bob Jacobsen Copyright 2009
 */
public class NamedBeanHandle<T extends NamedBean> implements java.io.Serializable {

    public NamedBeanHandle(@Nonnull String name, @Nonnull T bean) {
        this.name = name;
        this.bean = bean;
    }

    @CheckReturnValue
    public String getName() {
        return name;
    }

    @CheckReturnValue
    public T getBean() {
        return bean;
    }

    public void setBean(@Nonnull T bean) {
        this.bean = bean;
    }

    public void setName(String name) {
        this.name = name;
    }

    String name;
    T bean;

    @Override
    @CheckReturnValue
    public String toString() {
        return "NamedBeanHandle named \""+name+"\" for system name \""+bean.getSystemName()+"\"";
    }
    
    @Override
    @CheckReturnValue
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (!(getClass() == obj.getClass())) {
            return false;
        } else {
            NamedBeanHandle<?> tmp = (NamedBeanHandle<?>) obj;
            if (!tmp.getName().equals(this.getName())) {
                return false;
            }
            if (tmp.getBean() != this.getBean()) {
                return false;
            }
        }
        return true;
    }

    @Override
    @CheckReturnValue
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.getBean() != null ? this.getBean().hashCode() : 0);
        hash = 37 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
        return hash;
    }

}
