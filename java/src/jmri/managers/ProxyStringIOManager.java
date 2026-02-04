package jmri.managers;

import javax.annotation.Nonnull;
import jmri.Manager;
import jmri.StringIO;
import jmri.StringIOManager;

/**
 * Implementation of a StringIOManager that can serve as a proxy for multiple
 * system-specific implementations.
 *
 * @author  Bob Jacobsen      Copyright (C) 2010, 2018 2024
 * @author  Dave Duchamp      Copyright (C) 2004
 * @author  Daniel Bergqvist  Copyright (C) 2020
 */
public class ProxyStringIOManager extends AbstractProvidingProxyManager<StringIO>
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

    /** {@inheritDoc} */
    @Override
    @Nonnull
    protected StringIO makeBean(Manager<StringIO> manager, String systemName, String userName) throws IllegalArgumentException {
        var retval = ((StringIOManager) manager).newStringIO(systemName, userName);
        log.trace("makeBean returns {}", retval);
        return retval;
    }

    /** {@inheritDoc} */
    @Override
    public StringIO getStringIO(@Nonnull String name) {
        return super.getNamedBean(name);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public StringIO newStringIO(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        return newNamedBean(systemName, userName);
    }

    @Override
    @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameStringIOs" : "BeanNameStringIO");
    }

    @Override
    @Nonnull
    public StringIO provideStringIO(@Nonnull String sName) throws IllegalArgumentException {
        return super.provideNamedBean(sName);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public StringIO provide(@Nonnull String name) throws IllegalArgumentException { 
        return provideStringIO(name); 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<StringIO> getNamedBeanClass() {
        return StringIO.class;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProxyStringIOManager.class);

}
