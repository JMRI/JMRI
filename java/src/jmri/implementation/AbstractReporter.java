package jmri.implementation;

import jmri.Reporter;

/**
 * Abstract base for the Reporter interface.
 * <p>
 * Implements the parameter binding support.
 * <p>
 * Note that we consider it an error for there to be more than one object that
 * corresponds to a particular physical Reporter on the layout.
 *
 * Abstract class providing the basic logic of the Reporter
 * interface
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Matthew Harris Copyright (C) 2011
 */
public abstract class AbstractReporter extends AbstractNamedBean implements Reporter {

    public AbstractReporter(String systemName) {
        super(systemName);
    }

    public AbstractReporter(String systemName, String userName) {
        super(systemName, userName);
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameReporter");
    }
    
    @Override
    public Object getCurrentReport() {
        return _currentReport;
    }

    @Override
    public Object getLastReport() {
        return _lastReport;
    }

    /**
     * Provide a general method for updating the report.
     */
    @Override
    public void setReport(Object r) {
        if (r == _currentReport) {
            return;
        }
        Object old = _currentReport;
        Object oldLast = _lastReport;
        _currentReport = r;
        if (r != null) {
            _lastReport = r;
            // notify
            firePropertyChange(PROPERTY_LAST_REPORT, oldLast, _lastReport);
        }
        // notify
        firePropertyChange(PROPERTY_CURRENT_REPORT, old, _currentReport);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Default implementation is a no-op for generic reporters.
     */
    @Override
    public void notifySeenElsewhere(jmri.IdTag tag, Reporter newReporter) {
        // Default no-op
    }

    /**
     * Helper function to notify that the collection property has been updated.
     * Always uses null as the previous value as the collection might be the same object.
     * <p>
     * This should only be used if we don't have a change in the current report. If notify(tag) is
     * already called, there is no need to additionally call this.
     */
    public void notifyCollectionUpdated() {
        if (this instanceof jmri.CollectingReporter) {
            firePropertyChange(Reporter.PROPERTY_COLLECTION, null, ((jmri.CollectingReporter) this).getCollection());
        }
    }

    // internal data members
    protected Object _lastReport = null;
    protected Object _currentReport = null;

}
