package jmri.util;

/**
 * Utility class for managing access to a NamedBean
 *
 * @author Bob Jacobsen Copyright 2009
 */
public class NamedBeanHandle<T> implements java.io.Serializable {

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

    String name;
    T bean;
}
