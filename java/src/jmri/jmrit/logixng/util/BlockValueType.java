package jmri.jmrit.logixng.util;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.Block;

/**
 * Which of a Block's values a LogixNG item reads.
 * <p>
 * A Block's value is the train Block tracking has placed in it. Its allocated
 * value is the train Dispatcher has allocated it to, set when Dispatcher's
 * "Place train name in allocated Blocks" is on. Its display value is what
 * panels show: the value, or the allocated value when the Block has no value.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public enum BlockValueType {

    /** The train Block tracking has placed in the Block. */
    Value(Bundle.getMessage("BlockValueType_Value"), Block.PROPERTY_VALUE),

    /** The train the Block is allocated to. */
    AllocatedValue(Bundle.getMessage("BlockValueType_AllocatedValue"), Block.PROPERTY_ALLOCATED_VALUE),

    /** The value, or the allocated value when the Block has no value. */
    DisplayValue(Bundle.getMessage("BlockValueType_DisplayValue"),
            Block.PROPERTY_VALUE, Block.PROPERTY_ALLOCATED_VALUE);

    private final String _text;
    private final List<String> _propertyNames;

    private BlockValueType(String text, String... propertyNames) {
        _text = text;
        _propertyNames = List.of(propertyNames);
    }

    /**
     * Get this value of a Block.
     *
     * @param block the Block
     * @return the value, which may be null
     */
    @CheckForNull
    public Object getValue(@Nonnull Block block) {
        switch (this) {
            case AllocatedValue:
                return block.getAllocatedValue();
            case DisplayValue:
                return block.getDisplayValue();
            case Value:
            default:
                return block.getValue();
        }
    }

    /**
     * Get the names of the Block properties whose change can change this
     * value.
     *
     * @return the property names
     */
    @Nonnull
    public List<String> getPropertyNames() {
        return _propertyNames;
    }

    @Override
    public String toString() {
        return _text;
    }

}
