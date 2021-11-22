package jmri.implementation;

import java.beans.PropertyChangeEvent;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import jmri.ShutDownTask;

import jmri.util.LoggingUtil;

/**
 * Abstract ShutDownTask implementation.
 * <p>
 * This implementation provides a "doRun" property with a protected getter and
 * setter to allow subclasses to set the "doRun" property to true inside
 * {@link #call()} so that the property can be checked inside {@link #run()} to
 * determine if anything should be done during shut down.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @author Randall Wood Copyright 2020
 */
public abstract class AbstractShutDownTask implements ShutDownTask {

    private final String mName;
    private boolean doRun = false;

    /**
     * Constructor specifies the name
     *
     * @param name Name to give this task
     */
    public AbstractShutDownTask(String name) {
        this.mName = name;
    }

    /**
     * {@inheritDoc}
     *
     * This implementation merely sets the "doRun" property to true, and should
     * be overridden for any real checking. Note that overriding implementations
     * should call {@link #setDoRun(boolean)} correctly.
     */
    @Override
    public Boolean call() {
        doRun = true;
        return doRun;
    }

    @Override
    public String getName() {
        return mName;
    }

    /**
     * {@inheritDoc}
     *
     * Note that overriding implementations should call this implementation to set
     * the doRun property correctly.
     */
    @OverridingMethodsMustInvokeSuper
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("shuttingDown".equals(evt.getPropertyName()) && Boolean.FALSE.equals(evt.getNewValue())) {
            doRun = false;
        }
    }

    /**
     * Check if action should be taken in {@link #run()} method. This defaults
     * to false, although the default implementation of {@link #call()} sets
     * this to true.
     *
     * @return true if action should be taken; false otherwise
     */
    public boolean isDoRun() {
        return doRun;
    }

    /**
     * Set if action should be taken in {@link #run()} method. Overriding
     * implementations of {@link #call()} must call this to set
     * {@link #isDoRun()} to true.
       *
     * @param flag true if action should be taken; false otherwise
     */
    public void setDoRun(boolean flag) {
        doRun = flag;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractShutDownTask.class);
}
