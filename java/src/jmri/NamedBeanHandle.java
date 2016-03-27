package jmri;

/**
 * Utility class for managing access to a NamedBean
 *
 * @author Bob Jacobsen Copyright 2009
 */
public class NamedBeanHandle<T> implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5234423868376521202L;

    public NamedBeanHandle(String name, T bean) {
        this.name = name;
        this.bean = bean;
    }

    public String getName() {
        return name;
    }

    public T getBean() {
        return bean;
    }

    public void setBean(T bean) {
        this.bean = bean;
    }

    public void setName(String name) {
        this.name = name;
    }

    String name;
    T bean;

    @Override
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
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.getBean() != null ? this.getBean().hashCode() : 0);
        hash = 37 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
        return hash;
    }

}
