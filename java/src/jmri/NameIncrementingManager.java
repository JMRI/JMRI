package jmri;

import javax.annotation.Nonnull;

/**
 * Interface that indicates that a Manager class has a
 * {@link #getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix)} method that
 * can be called without arguments.
 *
 * @author Bob Jacobsen Copyright 2022
 */
public interface NameIncrementingManager {

    /**
     * Get the next valid NamedBean system name.
     * <p>
     * For example, if the system name of the provided NamedBean is IS10, the next valid name is IS11.
     * <p>
     * Pays no attention to whether the next NamedBean already exists or not, just works out the name.
     * Nor is there any guarantee that the return value can actually be created:
     * a provide() call on the return value can still perhaps fail in some circumstances.
     * <p>
     * In some cases, there is no clear next address.  In that case, a JmriException is thrown.
     * For example, OLCB has no concept of a "next" address; Internal sensors don't necessarily
     * have numeric suffixes.
     * <p>
     * @param  currentBean      The NamedBean who's system name that provides the base for "next"
     * @return                  The next valid system name
     * @throws JmriException    If unable to create a valid next address
     */
    @Nonnull
    public default String getNextValidSystemName(@Nonnull NamedBean currentBean) throws JmriException {
        String currentName = currentBean.getSystemName();
        int increment = ( currentBean instanceof Turnout ? ((Turnout)currentBean).getNumberOutputBits() : 1);
        String nextName = jmri.util.StringUtil.incrementLastNumberInString(currentName, increment);
        if (nextName==null) {
            throw new JmriException("No existing number found when incrementing " + currentName);
        }
        return nextName;

    }
}
