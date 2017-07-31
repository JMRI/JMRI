package jmri.implementation;

import java.util.HashSet;
import java.util.Set;
import jmri.InstanceInitializer;

public class AbstractInstanceInitializer implements InstanceInitializer {

    /**
     * The default implementation always throws an IllegalArgumentException. If
     * called by an overriding method, the last line of the overriding method
     * should be {@code return super.getDefault(type);}.
     *
     * {@inheritDoc }
     */
    @Override
    public <T> Object getDefault(Class<T> type) throws IllegalArgumentException {
        throw new IllegalArgumentException();
    }

    /**
     * The default implementation returns an empty set.
     *
     * {@inheritDoc }
     */
    @Override
    public Set<Class<?>> getInitalizes() {
        return new HashSet<>();
    }

}
