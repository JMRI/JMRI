package jmri.time.implementation;

import java.time.*;

import jmri.implementation.*;

import javax.annotation.Nonnull;

import jmri.NamedBean;
import jmri.time.TimeProvider;

/**
 * Abstract class providing the basic logic of the TimeProvider interface.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2009
 */
public abstract class AbstractTimeProvider extends AbstractNamedBean implements TimeProvider {

    public AbstractTimeProvider(String systemName) {
        super(systemName);
    }

    public AbstractTimeProvider(String systemName, String userName) {
        super(systemName, userName);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String getBeanType() {
        return Bundle.getMessage("BeanNameTimeProvider");
    }

    /** {@inheritDoc} */
    @Override
    public void setState(int s) throws jmri.JmriException {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public int getState() {
        return NamedBean.UNKNOWN;
    }

    protected void timeIsUpdated(LocalDateTime oldTime) {
        LocalDateTime newTime = getTime();
        long oldSeconds = oldTime.toEpochSecond(ZoneOffset.UTC);
        long newSeconds = newTime.toEpochSecond(ZoneOffset.UTC);
        if (newSeconds != oldSeconds) {
            long oldMinutes = oldSeconds / 60;
            long newMinutes = newSeconds / 60;
            firePropertyChange(TimeProvider.PROPERTY_CHANGE_SECONDS, oldSeconds, newSeconds);
            if (newMinutes != oldMinutes) {
                firePropertyChange(TimeProvider.PROPERTY_CHANGE_MINUTES, oldMinutes, newMinutes);
                firePropertyChange(TimeProvider.PROPERTY_CHANGE_DATETIME, oldTime, newTime);
            }
        }
    }


//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractClock.class);

}
