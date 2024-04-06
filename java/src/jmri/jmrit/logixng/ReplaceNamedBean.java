package jmri.jmrit.logixng;

import java.util.List;


/**
 * Classes that implements this interface allows other classes to replace a
 * NamedBean with another NamedBean for the class that implements this interface.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public interface ReplaceNamedBean {

    void getNamedBeans(List<NamedBeanReference> list);

    void replaceNamedBean(NamedBeanReference oldBean, NamedBeanReference newBean);

}
