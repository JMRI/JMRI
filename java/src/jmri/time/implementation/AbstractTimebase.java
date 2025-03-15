package jmri.time.implementation;

import java.beans.PropertyChangeListener;
import java.time.*;

import java.util.Date;

import jmri.*;
import jmri.implementation.AbstractNamedBean;
import jmri.time.*;

/**
 * Abstract implementation of Timebase.
 *
 * This class contains all the code that relates to TimeProvider.
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2007
 * @author Dave Duchamp Copyright (C) 2007. additions/revisions for handling one hardware clock
 * @author Daniel Bergqvist Copyright (C) 2025
 */
public abstract class AbstractTimebase extends AbstractNamedBean implements Timebase {


    public AbstractTimebase(String sysName) {
        super(sysName);
    }

    @Override
    public void setTime(Date d) {
        TimeProvider tp = InstanceManager.getDefault(TimeProviderManager.class)
                .getCurrentTimeProvider();
        if (tp instanceof TimeSetter) {
            TimeSetter ts = (TimeSetter)tp;
            if (ts.canSetTime()) {
                LocalTime time = LocalTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
                ts.setTime(time);
            } else {
                throw new UnsupportedOperationException("The current TimeProvider can not start/stop time");
            }
        } else {
            throw new UnsupportedOperationException("The current TimeProvider is not a TimeSetter: "
                    + (tp != null ? tp.getClass().getName() : null));
        }
    }

    @Override
    public void setTime(Instant i) {
        TimeProvider tp = InstanceManager.getDefault(TimeProviderManager.class)
                .getCurrentTimeProvider();
        if (tp instanceof TimeSetter) {
            TimeSetter ts = (TimeSetter)tp;
            if (ts.canSetTime()) {
                LocalTime time = LocalTime.ofInstant(i, ZoneId.systemDefault());
                ts.setTime(time);
            } else {
                throw new UnsupportedOperationException("The current TimeProvider can not start/stop time");
            }
        } else {
            throw new UnsupportedOperationException("The current TimeProvider is not a TimeSetter: "
                    + (tp != null ? tp.getClass().getName() : null));
        }
    }

    @Override
    public void userSetTime(Date d) {
        TimeProvider tp = InstanceManager.getDefault(TimeProviderManager.class)
                .getCurrentTimeProvider();
        if (tp instanceof TimeSetter) {
            TimeSetter ts = (TimeSetter)tp;
            if (ts.canSetTime()) {
                LocalTime time = LocalTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
                ts.setTime(time);
            } else {
                throw new UnsupportedOperationException("The current TimeProvider can not start/stop time");
            }
        } else {
            throw new UnsupportedOperationException("The current TimeProvider is not a TimeSetter: "
                    + (tp != null ? tp.getClass().getName() : null));
        }
    }

    @Override
    public Date getTime() {
        TimeProvider tp = InstanceManager.getDefault(TimeProviderManager.class)
                .getCurrentTimeProvider();
        return java.sql.Time.valueOf(tp.getTime().toLocalTime());
    }

    @Override
    public void setRun(boolean y) {
        TimeProvider tp = InstanceManager.getDefault(TimeProviderManager.class)
                .getCurrentTimeProvider();
        if (tp instanceof StartStopTimeProvider) {
            StartStopTimeProvider sstp = (StartStopTimeProvider)tp;
            if (sstp.canStartAndStop()) {
                if (y) {
                    sstp.start();
                } else {
                    sstp.stop();
                }
            } else {
                throw new UnsupportedOperationException("The current TimeProvider can not start/stop time");
            }
        } else {
            throw new UnsupportedOperationException("The current TimeProvider is not a StartStopTimeProvider: "
                    + (tp != null ? tp.getClass().getName() : null));
        }
    }

    @Override
    public boolean getRun() {
        return InstanceManager.getDefault(TimeProviderManager.class)
                .getCurrentTimeProvider().isRunning();
    }

    @Override
    public void setRate(double factor) throws TimebaseRateException {
        TimeProvider tp = InstanceManager.getDefault(TimeProviderManager.class)
                .getCurrentTimeProvider();
        if (tp instanceof CanSetRate) {
            if (tp.getRate() instanceof RateSetter) {
                CanSetRate csr = (CanSetRate)tp;
                RateSetter rs = (RateSetter)tp.getRate();
                if (csr.canSetRate()) {
                    rs.setRate(factor);
                } else {
                    throw new UnsupportedOperationException("The current TimeProvider can not start/stop time");
                }
            } else {
                throw new UnsupportedOperationException("The current TimeProvider rate is not a RateSetter: "
                        + (tp.getRate() != null ? tp.getRate().getClass().getName() : null));
            }
        } else {
            throw new UnsupportedOperationException("The current TimeProvider is not a CanSetRate: "
                    + (tp != null ? tp.getClass().getName() : null));
        }
    }

    @Override
    public void userSetRate(double factor) throws TimebaseRateException {
        TimeProvider tp = InstanceManager.getDefault(TimeProviderManager.class)
                .getCurrentTimeProvider();
        if (tp instanceof CanSetRate) {
            if (tp.getRate() instanceof RateSetter) {
                CanSetRate csr = (CanSetRate)tp;
                RateSetter rs = (RateSetter)tp.getRate();
                if (csr.canSetRate()) {
                    rs.setRate(factor);
                } else {
                    throw new UnsupportedOperationException("The current TimeProvider can not start/stop time");
                }
            } else {
                throw new UnsupportedOperationException("The current TimeProvider rate is not a RateSetter: "
                        + (tp.getRate() != null ? tp.getRate().getClass().getName() : null));
            }
        } else {
            throw new UnsupportedOperationException("The current TimeProvider is not a CanSetRate: "
                    + (tp != null ? tp.getClass().getName() : null));
        }
    }

    @Override
    public double getRate() {
        return InstanceManager.getDefault(TimeProviderManager.class)
                .getCurrentTimeProvider().getRate().getRate();
    }

    @Override
    public double userGetRate() {
        return InstanceManager.getDefault(TimeProviderManager.class)
                .getCurrentTimeProvider().getRate().getRate();
    }

    @Override
    public void addMinuteChangeListener(PropertyChangeListener l) {
        InstanceManager.getDefault(TimeProviderManager.class)
                .getMainTimeProviderHandler().addMinuteChangeListener(l);
    }

    @Override
    public void removeMinuteChangeListener(PropertyChangeListener l) {
        InstanceManager.getDefault(TimeProviderManager.class)
                .getMainTimeProviderHandler().removeMinuteChangeListener(l);
    }

    @Override
    public PropertyChangeListener[] getMinuteChangeListeners() {
        return InstanceManager.getDefault(TimeProviderManager.class)
                .getMainTimeProviderHandler().getMinuteChangeListeners();
    }


//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractTimebase.class);

}
