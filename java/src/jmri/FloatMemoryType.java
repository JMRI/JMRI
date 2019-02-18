package jmri;

/**
 * FloatMemoryType defines a Float stored in a Memory
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public class FloatMemoryType extends AbstractMemoryType implements MemoryType {

    private float _min = (float) 0.0;
    private float _max = (float) 1.0;
    private float _resolution = (float) 0.01;
    private int _numDecimals = 2;

    public float getMin() {
        return _min;
    }

    public void setMin(float min) {
        _min = min;
    }

    public float getMax() {
        return _max;
    }

    public void setMax(float max) {
        _max = max;
    }

    public float getResolution() {
        return _resolution;
    }

    public void setResolution(float resolution) {
        _resolution = resolution;
    }

    public int getNumDecimals() {
        return _numDecimals;
    }

    public void setNumDecimals(int numDecimals) {
        _numDecimals = numDecimals;
    }

    /** {@inheritDoc} */
    @Override
    public Object validate(Object value) {
        if (! (value instanceof Float)) {
            throw new IllegalArgumentException(
                    Bundle.getMessage("FloatMemoryType_InvalidType", value.getClass().getName()));
        }
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public String formatString(Object value) {
        return String.format(
                Bundle.getMessage("FloatMemoryType_FormatString", _numDecimals),
                ((Memory)value).getValue());
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return Bundle.getMessage("FloatMemoryType_Name");
    }

}
