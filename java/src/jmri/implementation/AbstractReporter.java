package jmri.implementation;

import jmri.ExtendedReport;
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
    public final void setReport(Object r) {
        // If extended reports are supported, then the method setExtendedReport() must be used instead.
        if (isExtendedReportsSupported()) throw new UnsupportedOperationException("Use setExtendedReport() instead");

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

    @Override
    public boolean isExtendedReportsSupported() {
//        return false;   // Disable extended reports for now
        return true;   // Assume yes for now
//        throw new UnsupportedOperationException("The implementing class needs to implement this method");
    }

    @Override
    public ExtendedReport getLastExtendedReport() {
        return _lastExtendedReport;
    }

    @Override
    public ExtendedReport getCurrentExtendedReport() {
        return _currentExtendedReport;
    }

    @Override
    public void setExtendedReport(Object report, ExtendedReport extendedReport) {
        if (!isExtendedReportsSupported()) {
            log.warn("This reporter does not support extended reports. Report: {}, Extended report: {}", report, extendedReport);
        }

        if (report != null) {
            if ((extendedReport != report)
                    && (extendedReport != ExtendedReport.NULL_REPORT)) {
                throw new IllegalArgumentException(
                        "extendedReport must be report or ExtendedReport.NULL_REPORT if report is not null");
            }
        } else {
            if (extendedReport != null) {
                throw new IllegalArgumentException("extendedReport must be null if report is null");
            }
        }
        if (report == _currentReport) {
            return;
        }
        Object old = _currentReport;
        Object oldLast = _lastReport;
        _currentReport = report;
        _currentExtendedReport = extendedReport;
        if (report != null) {
            _lastReport = report;
            _lastExtendedReport = extendedReport;
            // notify
            firePropertyChange("lastReport", oldLast, _lastReport);
        }
        // notify
        firePropertyChange("currentReport", old, _currentReport);
    }

    // internal data members
    protected Object _lastReport = null;
    protected Object _currentReport = null;
    
    private ExtendedReport _lastExtendedReport = null;
    private ExtendedReport _currentExtendedReport = null;


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractReporter.class);
}
