package jmri.managers;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import jmri.*;

/**
 * Implementation of a MeterManager that can serve as a proxy for multiple
 * system-specific implementations.
 *
 * @author  Bob Jacobsen      Copyright (C) 2010, 2018
 * @author  Dave Duchamp      Copyright (C) 2004
 * @author  Daniel Bergqvist  Copyright (C) 2020
 */
public class ProxyMeterManager extends AbstractProxyManager<Meter>
        implements MeterManager {
    
    private boolean muteUpdates = false;
    private final List<Class<? extends Meter>> registerBeans = new ArrayList<>();
    private final List<Manager<? extends NamedBean>> registerBeanManagers = new ArrayList<>();

    @Override
    public int getXMLOrder() {
        return jmri.Manager.METERS;
    }

    @Override
    protected AbstractManager<Meter> makeInternalManager() {
        return jmri.InstanceManager.getDefault(jmri.jmrix.internal.InternalSystemConnectionMemo.class).getMeterManager();
    }

    @Override
    @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameMeters" : "BeanNameMeter");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Meter> getNamedBeanClass() {
        return Meter.class;
    }

    /* {@inheritDoc} */
    @Override
    @CheckReturnValue
    @CheckForNull
    public Meter getBySystemName(@Nonnull String systemName) {
        Meter meter = super.getBySystemName(systemName);
        if (meter == null) {
            meter = initInternal().getBySystemName(systemName);
        }
        return meter;
    }

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public Meter getByUserName(@Nonnull String userName) {
        Meter meter = super.getByUserName(userName);
        if (meter == null) {
            meter = initInternal().getByUserName(userName);
        }
        return meter;
    }

    /**
     * Try to create a system manager.If this proxy manager is able to create
     * a system manager, the concrete class must implement this method.
     *
     * @param memo the system connection memo for this connection
     * @return the new manager or null if it's not possible to create the manager
     */
    @Override
    protected Manager<Meter> createSystemManager(@Nonnull SystemConnectionMemo memo) {
        MeterManager m = new jmri.managers.AbstractMeterManager(memo);
        InstanceManager.setMeterManager(m);
        return m;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);
        
        // When we add or remove the Light to the internal Meter manager,
        // we get a propertyChange for that.
        if (muteUpdates) return;
        
        if ("beans".equals(e.getPropertyName())) {
            
            for (Class<? extends Meter> clazz : registerBeans) {
                // A NamedBean is added
                if ((e.getNewValue() != null)
                        && clazz.isAssignableFrom(e.getNewValue().getClass())) {
                    Manager<Meter> internalManager = initInternal();
                    muteUpdates = true;
                    internalManager.register((Meter) e.getNewValue());
                    muteUpdates = false;
                }
                
                // A NamedBean is removed
                if ((e.getOldValue() != null)
                        && clazz.isAssignableFrom(e.getOldValue().getClass())) {
                    Manager<Meter> internalManager = initInternal();
                    muteUpdates = true;
                    internalManager.deregister((Meter) e.getOldValue());
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

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProxyMeterManager.class);

}
