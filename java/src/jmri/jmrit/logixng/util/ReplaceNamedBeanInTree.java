package jmri.jmrit.logixng.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.jmrit.logixng.*;


/**
 * Class that replaces a NamedBean with another in a part of a ConditionalNG tree.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class ReplaceNamedBeanInTree {

    @SuppressWarnings("unchecked") // Due to type erasure
    private void getSelectNamedBeans(Base base, List<LogixNG_SelectNamedBean<? extends NamedBean>> list)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

//        System.out.format("Class: %s%n", base.getClass().getName());

        for (Method m : base.getClass().getDeclaredMethods()) {
            if (m.getReturnType().equals(LogixNG_SelectNamedBean.class)) {
                Object result = m.invoke(base);
                if (! (result instanceof LogixNG_SelectNamedBean)) {
                    throw new RuntimeException("result is not a LogixNG_SelectNamedBean");
                }
                list.add((LogixNG_SelectNamedBean<? extends NamedBean>) result);
            }
        }
    }

    public List<LogixNG_SelectNamedBean<? extends NamedBean>> getSelectNamedBeans(Base base) throws Exception {
        List<LogixNG_SelectNamedBean<? extends NamedBean>> list = new ArrayList<>();

        base.forEntireTreeWithException((Base b) -> {
            if (b instanceof FemaleSocket) return;
            Base object = b;
            while (object instanceof MaleSocket) {
                object = ((MaleSocket)object).getObject();
            }
            getSelectNamedBeans(object, list);
        });

        return list;
    }

    public List<NamedBeanHandle<? extends NamedBean>> getNamedBeans(Base base) {
        List<NamedBeanHandle<? extends NamedBean>> list = new ArrayList<>();

        base.forEntireTree((Base b) -> {
//            getSelectNamedBeans(b, list);
        });

        return list;
    }

//    void getNamedBeans(List<NamedBeanReference> list);

//    void replaceNamedBean(NamedBeanReference oldBean, NamedBeanReference newBean);

}
