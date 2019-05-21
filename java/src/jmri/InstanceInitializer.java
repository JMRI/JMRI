package jmri;

import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Interface providing initialization of specific objects by default. This is
 * used to move references to specific subtypes out of the jmri package and into
 * more specialized packages.
 * <p>
 * Note that this is only needed when the object can't be created with a
 * no-arguments constructor. In that case, the
 * {@link InstanceManagerAutoDefault} mechanism is a better choice.
 * <p>
 * Instances of this class will normally be used only if they are annotated with
 * {@code @ServiceProvider(service = InstanceInitializer.class)}
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @since 2.9.4
 */
public interface InstanceInitializer {

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
     * @return the newly created default for the given class
     * @throws IllegalArgumentException if creating an instance of type is not
     *                                  supported by this InstanceInitalizer
     */
    @Nonnull
    public <T> Object getDefault(@Nonnull Class<T> type) throws IllegalArgumentException;

    /**
     * Get the set of classes for which this InstanceInitializer can provide
     * default instances for.
     *
     * @return the set of classes this InstanceInitalizer supports; if empty,
     *         {@link #getDefault(java.lang.Class)} will never be called.
     */
    @Nonnull
    public Set<Class<?>> getInitalizes();
}
