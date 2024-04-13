package jmri.jmrit.logixng.util;

import java.util.List;

import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.jmrit.logixng.NamedBeanType;

/**
 * Interface that allows NamedBeans to be replaced.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public interface ReplaceableNamedBean {

    public interface GetAndReplaceNamedBean {

        NamedBeanType getType();

        NamedBeanHandle<? extends NamedBean> get();

        void replace(NamedBeanHandle<? extends NamedBean> newBean);

//        void replaceNamedBean(NamedBeanHandle<? extends NamedBean> oldBean,
//                NamedBeanHandle<? extends NamedBean> newBean);

    }

    /**
     * Get all the GetAndReplaceNamedBean needed to update the NamedBeans used
     * directly or indirectly by this class.
     * @param list the list of GetAndReplaceNamedBean.
     */
    void getGetAndReplaceNamedBeans(List<GetAndReplaceNamedBean> list);

}
