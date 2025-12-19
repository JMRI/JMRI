package jmri.managers;

import jmri.time.TimeProvider;

import javax.annotation.Nonnull;

import jmri.*;
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

    private volatile TimeProviderManager _internalManager;

    @Override
    public void initialize() {
        TimeProvider internalTimeProvider = new InternalDateTime(makeSystemName("InternalTimeProvider")).init();
        register(internalTimeProvider);
        TimeProvider systemClock = new SystemDateTime(makeSystemName("SystemClock")).init();
        register(systemClock);
        MainTimeProviderHandler mtph = getMainTimeProviderHandler();
        mtph.setPrimaryTimeProvider(internalTimeProvider);
        mtph.setSecondaryTimeProvider(systemClock);
        mtph.setUsePrimaryTimeProvider(true);
//        mtph.setUsePrimaryTimeProvider(false);
    }

    @Override
    protected TimeProviderManager makeInternalManager() {
        // It has been observed on GitHub CI tests that the internal manager
        // has been created twice. We want to prevent that.
        if (_internalManager == null) {
            synchronized(this) {
                if (_internalManager == null) {
                    _internalManager = jmri.InstanceManager.getDefault(jmri.jmrix.internal.InternalSystemConnectionMemo.class).getTimeProviderManager();
                }
            }
        }
        return _internalManager;
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
        return Bundle.getMessage(plural ? "BeanNameTimeProviders" : "BeanNameTimeProvider");
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
        if (!jmri.InstanceManager.containsDefault(HasTimeProviderManager.class)) {
            throw new UnsupportedOperationException("TimeProviderManager not initialized by JUnitUtil");
        }
        return getMainTimeProviderHandler().getCurrentTimeProvider();
    }

    public interface HasTimeProviderManager {}

    // initialize logging
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProxyClockManager.class);

}
