package jmri.managers;

import jmri.*;
import jmri.time.TimeProvider;
import jmri.time.TimeProviderManager;

/**
 * Abstract implementation of time provider manager.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public abstract class AbstractTimeProviderManager extends AbstractManager<TimeProvider>
        implements TimeProviderManager {

    public AbstractTimeProviderManager(SystemConnectionMemo memo) {
        super(memo);
    }

    /** {@inheritDoc} */
    @Override
    public final char typeLetter() {
        return 'U';
    }

    /** {@inheritDoc} */
    @Override
    public final Class<TimeProvider> getNamedBeanClass() {
        return TimeProvider.class;
    }

    /** {@inheritDoc} */
    @Override
    public final int getXMLOrder() {
        return Manager.TIMEPROVIDERS;
    }

    /** {@inheritDoc} */
    @Override
    public final String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameTimeProviders" : "BeanNameTimeProvider");
    }

    @Override
    public final TimeProvider getCurrentTimeProvider() {
        return InstanceManager.getDefault(TimeProviderManager.class)
                .getMainTimeProviderHandler().getCurrentTimeProvider();
    }

    @Override
    public void dispose() {
        for (var tp : getNamedBeanSet()) {
            tp.dispose();
        }
        super.dispose();
    }

}
