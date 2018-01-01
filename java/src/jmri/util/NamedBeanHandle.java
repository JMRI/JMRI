package jmri.util;

import jmri.NamedBean;

/**
 * Utility class for managing access to a NamedBean
 *
 * @author Bob Jacobsen Copyright 2009
 * @param <T> the type of handled NamedBean
 */
public class NamedBeanHandle<T extends NamedBean> extends jmri.NamedBeanHandle<T> {

    public NamedBeanHandle(String name, T bean) {
        super(name, bean);
    }
}
