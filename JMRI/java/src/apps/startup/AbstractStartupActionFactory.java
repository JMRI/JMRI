package apps.startup;

import java.util.Locale;

/**
 * Abstract implementation of {@link apps.startup.StartupActionFactory} that
 * covers some boilerplate code for most implementations.
 *
 * @author Randall Wood (C) 2016
 */
abstract public class AbstractStartupActionFactory implements StartupActionFactory {

    /**
     * {@inheritDoc}
     *
     * This implementation calls
     * {@link #getTitle(java.lang.Class, java.util.Locale)} with the default
     * locale.
     *
     * @param clazz the class
     * @return the title
     * @throws IllegalArgumentException if the class is not supported by this
     *                                  factory
     */
    @Override
    public String getTitle(Class<?> clazz) throws IllegalArgumentException {
        return this.getTitle(clazz, Locale.getDefault());
    }

    /**
     * {@inheritDoc}
     *
     * This implementation returns an empty array.
     *
     * @param clazz the class
     * @return an empty array
     * @throws IllegalArgumentException if the class is not supported by this
     *                                  factory
     */
    @Override
    public String[] getOverriddenClasses(Class<?> clazz) throws IllegalArgumentException {
        return new String[0];
    }

}
