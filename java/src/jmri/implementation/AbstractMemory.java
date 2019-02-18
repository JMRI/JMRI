package jmri.implementation;

import jmri.Memory;
import jmri.MemoryType;

/**
 * Base for the Memory interface.
 * <P>
 * Implements the parameter binding support.
 * <p>
 * Memory system names are always upper case.
 *
 * @author Bob Jacobsen Copyright (C) 2004
 */
public abstract class AbstractMemory extends AbstractNamedBean implements Memory {

    public AbstractMemory(String systemName) {
        super(systemName.toUpperCase());
    }

    public AbstractMemory(String systemName, String userName) {
        super(systemName.toUpperCase(), userName);
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameMemory");
    }

    @Override
    public Object getValue() {
        return _current;
    }

    /** {@inheritDoc} */
    @Override
    public void setValue(Object v) {
        MemoryType memoryType = getMemoryType();
        if (memoryType != null) {
            v = memoryType.validate(v);
        }
        Object old = _current;
        _current = v;
        // notify
        firePropertyChange("value", old, _current);
    }

    /** {@inheritDoc} */
    @Override
    public MemoryType getMemoryType() {
        return _memoryType;
    }

    /** {@inheritDoc} */
    @Override
    public void setMemoryType(MemoryType memoryType) {
        _memoryType = memoryType;
        Object initialValue = _memoryType.getInitialValue();
        if (initialValue != null) {
            _current = _memoryType.validate(initialValue);
        }
    }

    // internal data members
    private Object _current = null;
    private MemoryType _memoryType = null;

}
