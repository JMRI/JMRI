package jmri.jmrit.logixng;

import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Interface providing initialization of null named beans.
 * <p>
 * Instances of this class will normally be used only if they are annotated with
 * {@code @ServiceProvider(service = InstanceInitializer.class)}
 * <p>
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public interface NullNamedBeanInitializer {

    /**
     * Provide a default instance of the given class.
     * <p>
     * <strong>Note</strong> calling this method twice for the same class should
     * not be expected to return the same instance; however, there is no
     * guarantee that the same instance will not be returned for two calls to
     * this method.
     *
     * @param <T>  the class to get the default for
     * @param type the class to get the default for
     * @param name the name of the new named bean
     * @return the newly created default for the given class
     * @throws IllegalArgumentException if creating an instance of type is not
     *                                  supported by this NullNamedBeanInitializer
     * <P>
     * @author Daniel Bergqvist Copyright (C) 2019
     */
    @Nonnull
    public <T> Object create(Class<T> type, @Nonnull String name)
            throws IllegalArgumentException;

    /**
     * Get the set of classes for which this NullNamedBeanInitializer can provide
     * default instances for.
     *
     * @return the set of classes this NullNamedBeanInitializer supports; if empty,
     *         {@link #create(java.lang.Class, java.lang.String)} will never be called.
     */
    @Nonnull
    public Set<Class<?>> getInitalizes();

}
