package apps.startup;

import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.spi.JmriServiceProviderInterface;

/**
 * Provide an SPI for registering potential startup actions with the
 * {@link apps.StartupActionsManager}.
 * <p>
 * Instances of this class need to be registered with a
 * {@link java.util.ServiceLoader}. The best way to register is include a file
 * {@code META-INF/services/apps.startup.StartupActionFactory} in the classpath
 * (preferably in a JAR with the action the instance supports).
 * <p>
 * {@link apps.startup.AbstractStartupActionFactory} provides an abstract base
 * class for creating factories that implements most of the boilerplate needed.
 *
 * @author Randall Wood (C) 2016;
 */
public interface StartupActionFactory extends JmriServiceProviderInterface {

    /**
     * Get the title for the given class using the default locale.
     *
     * @param clazz the class
     * @return the title
     * @throws IllegalArgumentException if the class is not supported by this
     *                                  factory
     */
    @Nonnull
    String getTitle(@Nonnull Class<?> clazz) throws IllegalArgumentException;

    /**
     * Get the title for the given class.
     *
     * @param clazz  the class
     * @param locale the desired locale for the title
     * @return the title in the given locale
     * @throws IllegalArgumentException if the class is not supported by this
     *                                  factory
     */
    @Nonnull
    String getTitle(@Nonnull Class<?> clazz, @Nonnull Locale locale) throws IllegalArgumentException;

    /**
     * Get the classes this factory supports.
     *
     * @return the supported classes or an empty array
     */
    @Nonnull
    Class<?>[] getActionClasses();

    /**
     * Get startup actions overridden by the given class. This is designed to
     * allow a new class to replace a deprecated class.
     * <p>
     * <strong>Note:</strong> the behavior is undefined if multiple classes
     * override a single class.
     *
     * @param clazz the overriding class
     * @return the overridden classes or an empty array
     * @throws IllegalArgumentException if the class is not supported by this
     *                                  factory
     */
    @Nonnull
    String[] getOverriddenClasses(@Nonnull Class<?> clazz) throws IllegalArgumentException;

}
