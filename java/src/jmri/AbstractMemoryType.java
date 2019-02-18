package jmri;

/**
 * Base for the MemoryType interface.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public abstract class AbstractMemoryType implements MemoryType {

    private Object _initialValue = null;

    @Override
    public Object getInitialValue() {
        return _initialValue;
    }

    @Override
    public void setInitialValue(Object initialValue) {
        _initialValue = validate(initialValue);
    }

}
