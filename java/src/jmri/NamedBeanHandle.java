package jmri;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Utility class for managing access to a NamedBean.
 * <p>
 * This associates a particular name (either the user name or system name,
 * typically) with a specific NamedBean. Later, when the user wants to do a
 * rename operation, this is used to decide whether this particular reference
 * should be renamed. Note, however, that these should only be created and
 * access via the {@link NamedBeanHandleManager} instance.
 *
 * @param <T> the class of the NamedBean
 * @see NamedBeanHandleManager
 * @see NamedBean
 *
 * @author Bob Jacobsen Copyright 2009
 */
public class NamedBeanHandle<T extends NamedBean> {

    /**
     * Create a handle to a particular bean accessed by a specific name.
     * <p>
     * Usually, defer to {@link NamedBeanHandleManager} to create these
     *
     * @param name the name for the handle
     * @param bean the bean to handle
     */
    public NamedBeanHandle(@Nonnull String name, @Nonnull T bean) {
        this.name = name;
        this.bean = bean;
    }

    @Nonnull
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
        return "NamedBeanHandle named \"" + name + "\" for system name \"" + bean.getSystemName() + "\"";
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
        return 259 + getName().hashCode();  // 259 is arbitrary offset constant
    }

}
