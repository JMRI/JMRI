// AbstractMemory.java

package jmri.implementation;

import jmri.*;

/**
 * Base for the Memory interface.
 * <P>
 * Implements the parameter binding support.
 * <P>
 * Note that we consider it an error for there to be more than one object
 * that corresponds to a particular physical Reporter on the layout.
 * <p>
 * Memory system names are always upper case.
 *
 * @author			Bob Jacobsen Copyright (C) 2004
 * @version			$Revision$
 */
public abstract class AbstractMemory extends AbstractNamedBean implements Memory, java.io.Serializable {

    public AbstractMemory(String systemName) {
        super(systemName.toUpperCase());
    }

    public AbstractMemory(String systemName, String userName) {
        super(systemName.toUpperCase(), userName);
    }
    
    public String getBeanType(){
        return Bundle.getMessage("BeanNameMemory");
    }

    public Object getValue() {return _current;}
    
    /**
     * Provide a general method for updating the report.
     */
    public void setValue(Object v) {
    	Object old = _current;
    	_current = v;
    	// notify
    	firePropertyChange("value", old, _current);
    }
    
    // internal data members
    private Object _current = null;

 }

/* @(#)AbstractMemory.java */
