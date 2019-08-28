package jmri.jmrit.logixng.implementation;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.ProvidingManager;
import jmri.jmrit.logixng.LogixNG_InstanceManager;

/**
 * A default implementation of the LogixNG_InstanceManager.
 */
public class DefaultLogixNG_InstanceManager implements LogixNG_InstanceManager {

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")  // Needed due to limitations of Java templates
    public <M extends Manager, N extends NamedBean> N get(
            Class<M> type, Class<N> clazz, String name) {
        
        return (N) InstanceManager.getDefault(type).getNamedBean(name);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")  // Needed due to limitations of Java templates
    public <M extends Manager, N extends NamedBean> N provide(
            Class<M> type, Class<N> clazz, String name) {
        
        if (type.isInstance(ProvidingManager.class)) {
            return ((ProvidingManager<N>)InstanceManager.getDefault(type))
                    .provide(name);
        } else {
            return null;
        }
    }

}
