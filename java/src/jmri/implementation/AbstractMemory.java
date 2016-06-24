package jmri.implementation;

import jmri.Memory;

/**
 * Base for the Memory interface.
 * <P>
 * Implements the parameter binding support.
 * <p>
 * Memory system names are always upper case.
 *
 * @author	Bob Jacobsen Copyright (C) 2004
 */
public abstract class AbstractMemory extends AbstractNamedBean implements Memory, java.io.Serializable {

    public AbstractMemory(String systemName) {
        super(systemName.toUpperCase());
    }

    public AbstractMemory(String systemName, String userName) {
        super(systemName.toUpperCase(), userName);
    }

    public String getBeanType() {
        return Bundle.getMessage("BeanNameMemory");
    }

    public Object getValue() {
        return _current;
    }

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
