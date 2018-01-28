package jmri;

/**
 * Implementation of NamedBeanPropertyDescriptor for true/false properties.
 * @author Balazs Racz Copyright (C) 2018
 */

public abstract class BooleanPropertyDescriptor extends NamedBeanPropertyDescriptor<Boolean> {
    public BooleanPropertyDescriptor(String key, boolean defVal) {
        super(key, defVal);
    }
}
