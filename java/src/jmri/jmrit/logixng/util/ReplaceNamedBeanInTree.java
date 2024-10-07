package jmri.jmrit.logixng.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ReplaceableNamedBean.GetAndReplaceNamedBean;


/**
 * Class that replaces a NamedBean with another in a part of a ConditionalNG tree.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class ReplaceNamedBeanInTree {

    private boolean implementsInterface(Class<?> clazz, Class<?> iface) {
        for (Class<?> c : clazz.getInterfaces()) {
            if (iface.equals(c)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked") // Due to type erasure
    private void getGetAndReplaceNamedBean(Object object, List<ReplaceableNamedBean.GetAndReplaceNamedBean> list)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        for (Method m : object.getClass().getDeclaredMethods()) {

            if (implementsInterface(m.getReturnType(), ReplaceableNamedBean.class)) {
                Object result = m.invoke(object);
                if (! (result instanceof ReplaceableNamedBean)) {
                    throw new RuntimeException("result is not a ReplaceableNamedBean");
                }
                // A LogixNG_SelectNamedBean have LogixNG_SelectNamedBean
                ((ReplaceableNamedBean)result).getGetAndReplaceNamedBeans(list);
            }
        }
    }

    public List<GetAndReplaceNamedBean> getSelectNamedBeans(Base base) throws Exception {
        List<GetAndReplaceNamedBean> list = new ArrayList<>();

        base.forEntireTreeWithException((Base b) -> {
            if (b instanceof FemaleSocket) return;
            Base object = b;
            while (object instanceof MaleSocket) {
                object = ((MaleSocket)object).getObject();
            }
            getGetAndReplaceNamedBean(object, list);
        });

        return list;
    }

    public List<NamedBeanHandle<NamedBean>> getNamedBeans(Base base) {
        List<NamedBeanHandle<NamedBean>> list = new ArrayList<>();

        base.forEntireTree((Base b) -> {
//            getSelectNamedBeans(b, list);
        });

        return list;
    }

    public void replaceNamedBeans(
            List<LogixNG_SelectNamedBean<NamedBean>> selectNamedBeans,
            Map<NamedBeanHandle<NamedBean>, NamedBeanHandle<NamedBean>> replacements) {

        for (var selectNamedBean : selectNamedBeans) {
            var oldHandle = selectNamedBean.getNamedBean();
            if (oldHandle != null) {
                var newHandle = replacements.get(oldHandle);
                selectNamedBean.setNamedBean(newHandle);
            }
        }

    }

//    void getNamedBeans(List<NamedBeanReference> list);

//    void replaceNamedBean(NamedBeanReference oldBean, NamedBeanReference newBean);

}
