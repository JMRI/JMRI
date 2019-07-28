package jmri.implementation;

import jmri.Memory;

/**
 * Base for the Memory interface.
 * <p>
 * Implements the parameter binding support.
 *
 * @author Bob Jacobsen Copyright (C) 2004
 */
public abstract class AbstractMemory extends AbstractNamedBean implements Memory {

    public AbstractMemory(String systemName) {
        super(systemName);
    }

    public AbstractMemory(String systemName, String userName) {
        super(systemName, userName);
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameMemory");
    }

    @Override
    public Object getValue() {
        return _current;
    }

    /**
     * Provide a general method for updating the report.
     */
    @Override
    public void setValue(Object v) {
        Object old = _current;
        _current = v;
        // notify
        firePropertyChange("value", old, _current);
    }

    // internal data members
    private Object _current = null;

}
