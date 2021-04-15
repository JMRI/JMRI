package jmri.jmrix.can.cbus;

import java.beans.PropertyChangeEvent;
import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Turnout;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.managers.AbstractTurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement TurnoutManager for CAN CBUS systems.
 * <p>
 * System names are "MT+n;-m", where M is the user configurable system prefix,
 * n and m are the events (signed for on/off, separated by ;).
 * <p>
 * Turnouts must be explicitly created, they are not polled.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @since 2.3.1
 */
public class CbusTurnoutManager extends AbstractTurnoutManager {

    /**
     * Ctor using a given system connection memo
     * @param memo System Connection
     */
    public CbusTurnoutManager(CanSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    protected Turnout createNewTurnout(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        String addr = systemName.substring(getSystemPrefix().length() + 1);
        // first, check validity
        String newAddress;
        try {
            newAddress = CbusAddress.validateSysName(addr);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            throw e;
        }
        // OK, make
        Turnout t = new CbusTurnout(getSystemPrefix(), newAddress, ((CanSystemConnectionMemo)getMemo()).getTrafficController());
        t.setUserName(userName);
        return t;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) {
        // first, check validity
        String newAddress;
        try {
            newAddress = CbusAddress.validateSysName(curAddress);
         } catch (IllegalArgumentException e) {
            newAddress = curAddress;
        }
        return prefix + typeLetter() + newAddress;
    }
    
    /**
     * Only increments by 1, which is fine for CBUS Turnouts.
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    protected String getIncrement(String curAddress, int increment) throws JmriException {
        return CbusAddress.getIncrement(curAddress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull Locale locale) {
        validateSystemNamePrefix(name, locale);
        try {
            CbusAddress.validateSysName(name.substring(getSystemNamePrefix().length()));
        } catch (IllegalArgumentException ex) {
            throw new jmri.NamedBean.BadSystemNameException(locale, "InvalidSystemNameCustom", ex.getMessage());
        }
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        String addr;
        try {
            addr = systemName.substring(getSystemPrefix().length() + 1); // get only the address part
        } catch (StringIndexOutOfBoundsException e){
            return NameValidity.INVALID;
        }
        try {
            CbusAddress.validateSysName(addr);
        } catch (IllegalArgumentException e){
            return NameValidity.INVALID;
        }
        return NameValidity.VALID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);
        if (e.getPropertyName().equals("inverted")) {
            firePropertyChange("beaninverted", null, null); //IN18N
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CbusTurnoutManager.class);

}
