package jmri.managers;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import jmri.*;

/**
 * Implementation of a AnalogIOManager that can serve as a proxy for multiple
 * system-specific implementations.
 *
 * @author  Bob Jacobsen      Copyright (C) 2010, 2018
 * @author  Dave Duchamp      Copyright (C) 2004
 * @author  Daniel Bergqvist  Copyright (C) 2020
 */
public class ProxyAnalogIOManager extends AbstractProxyManager<AnalogIO>
        implements AnalogIOManager {
    
    private boolean muteUpdates = false;
    private final List<Class<? extends AnalogIO>> registerBeans = new ArrayList<>();
    private final List<Manager<? extends NamedBean>> registerBeanManagers = new ArrayList<>();

    @Nonnull
    public ProxyAnalogIOManager init() {
        // Note that not all lights in LightManager are VariableLight.
        addBeanType(VariableLight.class, InstanceManager.getDefault(LightManager.class));
        return this;
    }

    @Override
    public int getXMLOrder() {
        return jmri.Manager.ANALOGIOS;
    }

    @Override
    protected AbstractManager<AnalogIO> makeInternalManager() {
        return jmri.InstanceManager.getDefault(jmri.jmrix.internal.InternalSystemConnectionMemo.class).getAnalogIOManager();
    }

    @Override
    @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameAnalogIOs" : "BeanNameAnalogIO");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<AnalogIO> getNamedBeanClass() {
        return AnalogIO.class;
    }

    /* {@inheritDoc} */
    @Override
    @CheckReturnValue
    @CheckForNull
    public AnalogIO getBySystemName(@Nonnull String systemName) {
        AnalogIO analogIO = super.getBySystemName(systemName);
        if (analogIO == null) {
            analogIO = initInternal().getBySystemName(systemName);
        }
        return analogIO;
    }

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public AnalogIO getByUserName(@Nonnull String userName) {
        AnalogIO analogIO = super.getByUserName(userName);
        if (analogIO == null) {
            analogIO = initInternal().getByUserName(userName);
        }
        return analogIO;
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);
        
        // When we add or remove the Light to the internal AnalogIO manager,
        // we get a propertyChange for that.
        if (muteUpdates) return;
        
        if ("beans".equals(e.getPropertyName())) {
            
            for (Class<? extends AnalogIO> clazz : registerBeans) {
                // A NamedBean is added
                if ((e.getNewValue() != null)
                        && clazz.isAssignableFrom(e.getNewValue().getClass())) {
                    Manager<AnalogIO> internalManager = initInternal();
                    muteUpdates = true;
                    internalManager.register((AnalogIO) e.getNewValue());
                    muteUpdates = false;
                }
                
                // A NamedBean is removed
                if ((e.getOldValue() != null)
                        && clazz.isAssignableFrom(e.getOldValue().getClass())) {
                    Manager<AnalogIO> internalManager = initInternal();
                    muteUpdates = true;
                    internalManager.deregister((AnalogIO) e.getOldValue());
                    muteUpdates = false;
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        super.dispose();
        for (Manager<? extends NamedBean> manager : registerBeanManagers) {
            manager.removePropertyChangeListener("beans", this);
        }
    }

    /**
     * Add a type of NamedBean, for example VariableLight, that should be also registred in AnalogIOManager.
     * @param clazz the NamedBean class that should be registered in this manager
     * @param manager the manager that managers the NamedBeans of type clazz
     */
    @Override
    public void addBeanType(Class<? extends AnalogIO> clazz, Manager<? extends NamedBean> manager) {
        registerBeans.add(clazz);
        manager.addPropertyChangeListener("beans", this);
    }

    /**
     * Remove a type of NamedBean, for example VariableLight, from beeing registred in AnalogIOManager.
     * @param clazz the NamedBean class that should be registered in this manager
     * @param manager the manager that managers the NamedBeans of type clazz
     */
    @Override
    public void removeBeanType(Class<? extends AnalogIO> clazz, Manager<? extends NamedBean> manager) {
        manager.removePropertyChangeListener("beans", this);
        registerBeans.remove(clazz);
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProxyAnalogIOManager.class);

}
