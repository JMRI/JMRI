package jmri;

import java.util.Set;

/**
 * Interface providing initialization of specific objects by default. This is
 * used to move references to specific subtypes out of the jmri package and into
 * e.g. jmri.managers.
 * <P>
 * Note that this is only needed when the object can't be created with a
 * no-arguments construtor. In that case, the {@link InstanceManagerAutoDefault}
 * mechanism is a better choice.
 *
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @since 2.9.4
 */
public interface InstanceInitializer {

    /**
     * Provide the default for the given class.
     *
     * @param <T>  the class to get the default for
     * @param type the class to get the default for
     * @return the newly created default for the given class
     */
    public <T> Object getDefault(Class<T> type) throws IllegalArgumentException;

    /**
     * Get the set of classes for which this InstanceInitializer can provide
     * default instances for.
     *
     * @return the set of classes this InstanceInitalizer supports
     */
    public Set<Class<?>> getInitalizes();
}
