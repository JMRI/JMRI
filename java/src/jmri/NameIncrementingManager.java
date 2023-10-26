package jmri;

import javax.annotation.Nonnull;
import javax.annotation.CheckReturnValue;

/**
 * Interface that indicates that a Manager class
 * capable of providing the
 * next system name
 * after the name of a given NamedBean.
 * <p>
 * This is used for e.g. providing a range of NamedBeans
 *
 * @author Bob Jacobsen Copyright 2022
 */
public interface NameIncrementingManager {

    /**
     * Determines if it is possible to add a range of NamedBeans in numerical
     * order for a particular system implementation.
     * <p>
     * Default is not providing this service.  Systems should override this
     * method if they do provide the service.
     *
     * @param systemName the system name to check against; appears to be ignored
     *                   in all implementations
     * @return true if possible; false otherwise
     */
    @CheckReturnValue
    default boolean allowMultipleAdditions(@Nonnull String systemName) {
        return false;
    }

    /**
     * Get the next valid NamedBean system name.
     * <p>
     * For example, if the system name of the provided NamedBean is IS10, the next valid name is IS11.
     * <p>
     * This should not be called if {@link #allowMultipleAdditions} returns false.
     * <p>
     * Pays no attention to whether the next NamedBean already exists or not, just works out the name.
     * Nor is there any guarantee that the return value can actually be created:
     * a provide() call on the return value can still perhaps fail in some circumstances.
     * <p>
     * In some cases, there is no clear next address.  In that case, a JmriException is thrown.
     * For example, some systems have no concept of a "next" address; Internal sensors don't necessarily
     * have numeric suffixes; etc.
     * <p>
     * Default implementation works for names of the form (prefix)(type letter)(numeric string) by
     * incrementing the numeric string as needed.
     *
     * @param  currentBean      The NamedBean who's system name that provides the base for "next"
     * @return                  The next valid system name
     * @throws JmriException    If unable to create a valid next address
     */
    @Nonnull
    @CheckReturnValue
    default String getNextValidSystemName(@Nonnull NamedBean currentBean) throws JmriException {
        if (!allowMultipleAdditions(currentBean.getSystemName())) throw new UnsupportedOperationException("Not supported");

        String currentName = currentBean.getSystemName();

        int increment = ( currentBean instanceof VariableControlSpanBean ? ((VariableControlSpanBean)currentBean).getNumberControlBits() : 1);

        String nextName = jmri.util.StringUtil.incrementLastNumberInString(currentName, increment);

        if (nextName==null) {
            throw new JmriException("No existing number found when incrementing " + currentName);
        }
        return nextName;

    }
}
