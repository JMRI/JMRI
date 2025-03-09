package jmri.managers;

import jmri.time.TimeProvider;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.time.implementation.SystemDateTime;
import jmri.time.TimeProviderManager;
import jmri.time.MainTimeProviderHandler;

/**
 * Implementation of a TimeProviderManager that can serve as a proxy for multiple
 * system-specific implementations.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2010
 */
public class ProxyTimeProviderManager extends AbstractProxyManager<TimeProvider>
        implements TimeProviderManager, InstanceManagerAutoInitialize {

    @Override
    public void initialize() {
        TimeProvider tp = new SystemDateTime(makeSystemName("SYSTEMCLOCK"));
        register(tp);
        MainTimeProviderHandler mtph = getMainTimeProviderHandler();
        mtph.setPrimaryTimeProvider(tp);
        mtph.setSecondaryTimeProvider(tp);
    }

    @Override
    protected TimeProviderManager makeInternalManager() {
        return jmri.InstanceManager.getDefault(jmri.jmrix.internal.InternalSystemConnectionMemo.class).getTimeProviderManager();
    }

    /** {@inheritDoc} */
    @Override
    public final int getXMLOrder() {
        return Manager.CLOCKS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public final String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameClocks" : "BeanNameClock");
    }

    /** {@inheritDoc} */
    @Override
    public final Class<TimeProvider> getNamedBeanClass() {
        return TimeProvider.class;
    }

    @Override
    public MainTimeProviderHandler getMainTimeProviderHandler() {
        return makeInternalManager().getMainTimeProviderHandler();
    }

    @Override
    public final TimeProvider getCurrentTimeProvider() {
        return getMainTimeProviderHandler().getCurrentTimeProvider();
    }

    // initialize logging
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProxyClockManager.class);

}
