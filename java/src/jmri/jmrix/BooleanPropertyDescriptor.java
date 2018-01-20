package jmri.jmrix;

import jmri.NamedBeanPropertyDescriptor;

/**
 * Implementation of NamedBeanPropertyDescriptor for true/false properties.
 * @author Balazs Racz Copyright (C) 2018
 */

public abstract class BooleanPropertyDescriptor extends NamedBeanPropertyDescriptor {
    public BooleanPropertyDescriptor(String key, boolean defVal) {
        super(key, Boolean.class, Boolean.valueOf(defVal));
    }

    @Override
    public String renderProperty(Object value) {
        return  value.toString();
    }

    @Override
    public Object parseProperty(String value) {
        return Boolean.parseBoolean(value);
    }
}
