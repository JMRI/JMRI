package jmri.jmrix;

import jmri.NamedBean;
import jmri.NamedBeanPropertyDescriptor;

/**
 * Implementation of NamedBeanPropertyDescriptor for true/false properties.
 * Created by bracz on 1/19/18.
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
