// AbstractReporter.java

package jmri;

/**
 * Abstract base for the Reporter interface.
 * <P>
 * Implements NONE feedback, where
 * the KnownState and CommandedState track each other. If you want to
 * implement some other feedback, override and modify setCommandedState()
 * here.
 * <P>
 * Implements the parameter binding support.
 * <P>
 * Note that we consider it an error for there to be more than one object
 * that corresponds to a particular physical Reporter on the layout.
 *
 * Description:		Abstract class providing the basic logic of the Reporter interface
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.1 $
 */
public abstract class AbstractReporter extends AbstractNamedBean implements Reporter, java.io.Serializable {

    public AbstractReporter(String systemName) {
        super(systemName);
    }

    public AbstractReporter(String systemName, String userName) {
        super(systemName, userName);
    }

    public Object getCurrentReport() {return _lastReport;}

    public Object getLastReport() {return _currentReport;}
    
    /**
     * Provide a general method for updating the report.
     */
    protected void setReport(Object r) {
    	Object old = _currentReport;
    	_currentReport = r;
    	if (r != null) _lastReport = r;
    	// notify
    	firePropertyChange("currentReport", _currentReport, old);
    }
    
    // internal data members
    private Object _lastReport = "";
    private Object _currentReport = null;

 }

/* @(#)AbstractReporter.java */
