package jmri.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * {@link java.util.Properties} implementation that enumerates properties sorted
 * by key.
 *
 * See
 * <a href="http://stackoverflow.com/a/3253071/176160">http://stackoverflow.com/a/3253071/176160</a>
 *
 * @author Randall Wood
 */
@API(status = EXPERIMENTAL)
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
