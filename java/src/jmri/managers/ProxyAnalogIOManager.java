package jmri.managers;

import javax.annotation.Nonnull;
import jmri.AnalogIO;
import jmri.AnalogIOManager;

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

    public ProxyAnalogIOManager() {
        super();
    }

    @Override
    public int getXMLOrder() {
        return jmri.Manager.ANALOG_IO;
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

}
