package jmri.jmrix.marklin;

import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Turnout;

/**
 * Implement turnout manager for Marklin systems.
 * <p>
 * System names use an optional protocol prefix followed by a 1-based address:
 * <ul>
 *   <li>{@code MTDCC45}  DCC accessory address 45</li>
 *   <li>{@code MTMM123}  MM/MM2 accessory address 123</li>
 *   <li>{@code MTCAN200} raw CAN wire address 200 (vendor/club range)</li>
 *   <li>{@code MT45}     legacy unprefixed, treated as DCC</li>
 * </ul>
 * <p>
 * Based on work by Bob Jacobsen
 *
 * @author Kevin Dickerson Copyright (C) 2012
 * @author Ralf Lang
 */
public class MarklinTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    /** Regex for the address portion after stripping system-prefix + type letter. */
    private static final java.util.regex.Pattern ADDRESS_PATTERN =
            MarklinAccessoryAddress.ADDRESS_PATTERN;

    public MarklinTurnoutManager(MarklinSystemConnectionMemo memo) {
        super(memo);
        tc = memo.getTrafficController();
    }

    private final MarklinTrafficController tc;

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public MarklinSystemConnectionMemo getMemo() {
        return (MarklinSystemConnectionMemo) memo;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Accepts {@code MM123}, {@code DCC45}, {@code CAN200} or plain {@code 123}
     * Unprefixed defaults to DCC for BC reasons.
     * The prefix and number are passed through verbatim.
     * The full system name becomes e.g. {@code MTDCC123}.
     */
    @Override
    @Nonnull
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        if (!ADDRESS_PATTERN.matcher(curAddress).matches()) {
            throw new JmriException("Invalid Marklin turnout address '" + curAddress
                    + "'. Use MM<n>, DCC<n>, CAN<n>, or plain <n> (defaults to DCC).");
        }
        return prefix + typeLetter() + curAddress;
    }

    /** {@inheritDoc} */
    @Override
    public jmri.Manager.NameValidity validSystemNameFormat(@Nonnull String systemName) {
        String addr = systemName.substring(getSystemPrefix().length() + 1); // strip e.g. "MT"
        return ADDRESS_PATTERN.matcher(addr).matches()
                ? jmri.Manager.NameValidity.VALID : jmri.Manager.NameValidity.INVALID;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String systemName, @Nonnull java.util.Locale locale)
            throws jmri.NamedBean.BadSystemNameException {
        systemName = validateSystemNamePrefix(systemName, locale);
        if (validSystemNameFormat(systemName) != jmri.Manager.NameValidity.VALID) {
            throw new jmri.NamedBean.BadSystemNameException(locale,
                    "InvalidSystemNameNotInteger", systemName, getSystemNamePrefix());
        }
        return systemName;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    protected Turnout createNewTurnout(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        Turnout t = new MarklinTurnout(systemName, getSystemPrefix(), tc);
        t.setUserName(userName);
        return t;
    }

    /** {@inheritDoc} */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("TurnoutEntryToolTip");
    }

    /** {@inheritDoc} */
    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

}
