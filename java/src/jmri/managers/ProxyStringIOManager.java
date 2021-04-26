package jmri.managers;

import javax.annotation.Nonnull;
import jmri.StringIO;
import jmri.StringIOManager;

/**
 * Implementation of a StringIOManager that can serve as a proxy for multiple
 * system-specific implementations.
 *
 * @author  Bob Jacobsen      Copyright (C) 2010, 2018
 * @author  Dave Duchamp      Copyright (C) 2004
 * @author  Daniel Bergqvist  Copyright (C) 2020
 */
public class ProxyStringIOManager extends AbstractProxyManager<StringIO>
        implements StringIOManager {

    public ProxyStringIOManager() {
        super();
    }

    @Override
    public int getXMLOrder() {
        return jmri.Manager.STRINGIOS;
    }

    @Override
    protected AbstractManager<StringIO> makeInternalManager() {
        return jmri.InstanceManager.getDefault(jmri.jmrix.internal.InternalSystemConnectionMemo.class).getStringIOManager();
    }

    @Override
    @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameStringIOs" : "BeanNameStringIO");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<StringIO> getNamedBeanClass() {
        return StringIO.class;
    }

}
