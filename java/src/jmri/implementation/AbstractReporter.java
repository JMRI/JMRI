// AbstractReporter.java
package jmri.implementation;

import jmri.Reporter;

/**
 * Abstract base for the Reporter interface.
 * <P>
 * Implements the parameter binding support.
 * <P>
 * Note that we consider it an error for there to be more than one object that
 * corresponds to a particular physical Reporter on the layout.
 *
 * Description:	Abstract class providing the basic logic of the Reporter
 * interface
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @author Matthew Harris Copyright (C) 2011
 * @version	$Revision$
 */
public abstract class AbstractReporter extends AbstractNamedBean implements Reporter, java.io.Serializable {

    public AbstractReporter(String systemName) {
        super(systemName.toUpperCase());
    }

    public AbstractReporter(String systemName, String userName) {
        super(systemName.toUpperCase(), userName);
    }

    public String getBeanType() {
        return Bundle.getMessage("BeanNameReporter");
    }

    public Object getCurrentReport() {
        return _currentReport;
    }

    public Object getLastReport() {
        return _lastReport;
    }

    /**
     * Provide a general method for updating the report.
     */
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
    private Object _lastReport = null;
    private Object _currentReport = null;

}

/* @(#)AbstractReporter.java */
