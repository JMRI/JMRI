package jmri.managers;

import jmri.time.TimeProvider;

import javax.annotation.Nonnull;

import jmri.InstanceManagerAutoInitialize;
import jmri.Manager;
import jmri.time.MainTimeProviderHandler;
import jmri.time.TimeProviderManager;
import jmri.time.implementation.InternalDateTime;
import jmri.time.implementation.SystemDateTime;

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
        TimeProvider internalTimeProvider = new InternalDateTime(makeSystemName("InternalTimeProvider"));
        register(internalTimeProvider);
        TimeProvider systemClock = new SystemDateTime(makeSystemName("SystemClock"));
        register(systemClock);
        MainTimeProviderHandler mtph = getMainTimeProviderHandler();
        mtph.setPrimaryTimeProvider(internalTimeProvider);
        mtph.setSecondaryTimeProvider(systemClock);
        mtph.setUsePrimaryTimeProvider(true);
    }

    @Override
    protected TimeProviderManager makeInternalManager() {
        return jmri.InstanceManager.getDefault(jmri.jmrix.internal.InternalSystemConnectionMemo.class).getTimeProviderManager();
    }

    /** {@inheritDoc} */
    @Override
    public final int getXMLOrder() {
        return Manager.TIMEPROVIDERS;
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
