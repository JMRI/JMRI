package jmri.implementation;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
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
    @Nonnull
    public <T> Object getDefault(@Nonnull Class<T> type) {
        throw new IllegalArgumentException();
    }

    /**
     * The default implementation returns an empty set.
     *
     * {@inheritDoc }
     */
    @Override
    @Nonnull
    public Set<Class<?>> getInitalizes() {
        return new HashSet<>();
    }

}
