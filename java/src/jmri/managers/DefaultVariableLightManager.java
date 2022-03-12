package jmri.managers;

import javax.annotation.*;
import java.beans.PropertyChangeEvent;

import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.Manager;
import jmri.VariableLight;
import jmri.VariableLightManager;
import jmri.SystemConnectionMemo;

/**
 * Default implementation of a VariableLightManager.
 *
 * @author Bob Jacobsen       Copyright (C) 2004
 * @author Daniel Bergqvist   Copyright (C) 2020
 */
public class DefaultVariableLightManager extends AbstractManager<VariableLight>
        implements VariableLightManager {

    /**
     * Create a new VariableLightManager instance.
     * 
     * @param memo the system connection
     */
    public DefaultVariableLightManager(SystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * Initializes a new VariableLightManager instance.
     * @return itself
     */
    public DefaultVariableLightManager init() {
        LightManager lm = InstanceManager.getDefault(LightManager.class);
        lm.addPropertyChangeListener("beans", this);
        for (Light l : lm.getNamedBeanSet()) {
            if (l instanceof VariableLight) {
                super.register((VariableLight) l);
            }
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        super.dispose();
    }

    /** {@inheritDoc} */
    @Override
    public int getXMLOrder() {
        return Manager.LIGHTS;
    }

    /** {@inheritDoc} */
    @Override
    public char typeLetter() {
        return 'L';
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull 
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameVariableLights" : "BeanNameVariableLight");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<VariableLight> getNamedBeanClass() {
        return VariableLight.class;
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void register(@Nonnull VariableLight s) {
        throw new UnsupportedOperationException("Not supported. Use LightManager.register() instead");
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void deregister(@Nonnull VariableLight s) {
        throw new UnsupportedOperationException("Not supported. Use LightManager.deregister() instead");
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void deleteBean(@Nonnull VariableLight n, @Nonnull String property) {
        throw new UnsupportedOperationException("Not supported. Use LightManager.deleteBean() instead");
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);

        if ("beans".equals(e.getPropertyName())) {

            // A NamedBean is added
            if ((e.getNewValue() != null)
                    && (e.getNewValue() instanceof VariableLight)) {
                super.register((VariableLight) e.getNewValue());
            }

            // A NamedBean is removed
            if ((e.getOldValue() != null)
                    && (e.getOldValue() instanceof VariableLight)) {
                super.deregister((VariableLight) e.getOldValue());
            }
        }
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultVariableLightManager.class);

}
