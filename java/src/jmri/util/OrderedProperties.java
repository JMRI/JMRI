package jmri.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * {@Link java.util.Properties} implementation that enumerates properties sorted
 * by key.
 * 
 * See http://stackoverflow.com/a/3253071/176160
 * 
 * @author Randall Wood
 */
public class OrderedProperties extends Properties {

    @Override
    public Set<Object> keySet() {
        return Collections.unmodifiableSet(new TreeSet<Object>(super.keySet()));
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        return Collections.enumeration(new TreeSet<Object>(super.keySet()));
    }
}
