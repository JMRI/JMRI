package jmri;

/**
 * StringMemoryType defines a String stored in a Memory
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public class StringMemoryType extends AbstractMemoryType implements MemoryType {

    private int _maxLength = 0;  // 0 = unlimited length

    public int getMaxLenght() {
        return _maxLength;
    }

    public void setMaxLenght(int maxLenght) {
        _maxLength = maxLenght;
    }

    /** {@inheritDoc} */
    @Override
    public Object validate(Object value) {
        if (! (value instanceof String)) {
            throw new IllegalArgumentException(
                    Bundle.getMessage("StringMemoryType_InvalidType", value.getClass().getName()));
        }
        
        String s = (String)value;
        if ((_maxLength == 0) || (s.length() <= _maxLength)) {
            return s;
        } else {
            return s.substring(0, _maxLength);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String formatString(Object value) {
        return (String)value;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return Bundle.getMessage("StringMemoryType_Name");
    }

}
