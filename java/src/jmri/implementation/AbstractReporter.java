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
 * Description: Abstract class providing the basic logic of the Reporter
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
            firePropertyChange("lastReport", oldLast, _lastReport);
        }
        // notify
        firePropertyChange("currentReport", old, _currentReport);
    }

    // internal data members
    protected Object _lastReport = null;
    protected Object _currentReport = null;

}
