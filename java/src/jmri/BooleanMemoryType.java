package jmri;

/**
 * BooleanMemoryType defines a Boolean stored in a Memory
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public class BooleanMemoryType extends AbstractMemoryType implements MemoryType {

    /** {@inheritDoc} */
    @Override
    public Object validate(Object value) {
        if (! (value instanceof Boolean)) {
            throw new IllegalArgumentException(
                    Bundle.getMessage("BooleanMemoryType_InvalidType", value.getClass().getName()));
        }
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public String formatString(Object value) {
        if ((Boolean)value) {
            return Bundle.getMessage("BooleanMemoryType_True");
        } else {
            return Bundle.getMessage("BooleanMemoryType_False");
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return Bundle.getMessage("BooleanMemoryType_Name");
    }

}
