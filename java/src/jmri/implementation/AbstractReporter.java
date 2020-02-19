package jmri.implementation;

import jmri.IdTag;
import jmri.Reporter;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameReporter");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getCurrentReport() {
        return _currentReport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getLastReport() {
        return _lastReport;
    }

    /**
     * Provide a general method for updating the report.
     * {@inheritDoc}
     */
    @Override
    public void setReport(Object r) {
        
        Object old = _currentReport;
        Object oldLast = _lastReport;
        
        if ( dateChangedOnIdTag(r) ){
            old = null;
            oldLast = null;
        }
        else if (r == _currentReport) {
            return;
        }
        
        _currentReport = r;
        if (r != null) {
            _lastReport = r;
            // notify
            firePropertyChange("lastReport", oldLast, _lastReport);
        }
        // notify
        firePropertyChange("currentReport", old, _currentReport);
    }
    
    protected boolean dateChangedOnIdTag(Object r) {
        if ( _currentReport!=null && _currentReport instanceof IdTag ) {
            java.util.Date _lastSeenTime = null;
            if (r !=null && r instanceof IdTag) {
                _lastSeenTime = ((IdTag) r).getWhenLastSeen();
            }
            if (((IdTag)_currentReport).getWhenLastSeen() != _lastSeenTime ) {
                return true;
            }
        }
        return false;
    }

    // internal data members
    protected Object _lastReport = null;
    protected Object _currentReport = null;
    
    // private final static Logger log = LoggerFactory.getLogger(AbstractReporter.class);

}
