package jmri.jmrix.srcp;

import javax.annotation.Nonnull;
import jmri.Turnout;

/**
 * Implement TurnoutManager for SRCP systems.
 * <p>
 * System names are "DTnnn", where D is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
public class SRCPTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    public SRCPTurnoutManager(SRCPBusConnectionMemo memo) {
        super(memo);
    }

    /**
     *
     * @param memo the associated SystemConnectionMemo
     * @param bus the bus ID configured for this connection
     * @deprecated since 4.18 use {@link SRCPBusConnectionMemo#getBus()}
     */
    @Deprecated
    public SRCPTurnoutManager(SRCPBusConnectionMemo memo, int bus) {
        this(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public SRCPBusConnectionMemo getMemo() {
        return (SRCPBusConnectionMemo) memo;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    protected Turnout createNewTurnout(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        int addr;
        try {
            addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Failed to convert systemName '"+systemName+"' to a Turnout address");
        }
        Turnout t = new SRCPTurnout(addr, getMemo());
        t.setUserName(userName);
        return t;
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }
    
    /**
     * Validates to only numeric.
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull java.util.Locale locale) throws jmri.NamedBean.BadSystemNameException {
        return validateSystemNameFormatOnlyNumeric(name,locale);
    }

    // private final static Logger log = LoggerFactory.getLogger(SRCPTurnout.class);

}
