// AbstractMemory.java

package jmri;

/**
 * Base for the Memory interface.
 * <P>
 * Implements the parameter binding support.
 * <P>
 * Note that we consider it an error for there to be more than one object
 * that corresponds to a particular physical Reporter on the layout.
 *
 * @author			Bob Jacobsen Copyright (C) 2004
 * @version			$Revision: 1.1 $
 */
public abstract class AbstractMemory extends AbstractNamedBean implements Memory, java.io.Serializable {

    public AbstractMemory(String systemName) {
        super(systemName);
    }

    public AbstractMemory(String systemName, String userName) {
        super(systemName, userName);
    }

    public Object getValue() {return _current;}
    
    /**
     * Provide a general method for updating the report.
     */
    public void setValue(Object v) {
    	Object old = _current;
    	_current = v;
    	// notify
    	firePropertyChange("value", _current, old);
    }
    
    // internal data members
    private Object _current = null;

 }

/* @(#)AbstractMemory.java */
