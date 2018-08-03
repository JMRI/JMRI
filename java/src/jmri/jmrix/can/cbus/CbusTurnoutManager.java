package jmri.jmrix.can.cbus;

import jmri.JmriException;
import jmri.Turnout;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.managers.AbstractTurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CAN CBUS implementation of a TurnoutManager.
 * <p>
 * Turnouts must be manually created.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @since 2.3.1
 */
 
 
// TODO - getNextValidAddress
// TODO - Check for duplicates
 
 
 
public class CbusTurnoutManager extends AbstractTurnoutManager {

    public CbusTurnoutManager(CanSystemConnectionMemo memo) {
        this.memo = memo;
        String prefix = memo.getSystemPrefix();
    }

    CanSystemConnectionMemo memo;

    @Override
    public String getSystemPrefix() {
        return memo.getSystemPrefix();
    }
    
    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        String addr = systemName.substring(getSystemPrefix().length() + 1);
        // log.debug("42 createNewTurnout systemName from any format {} ", addr);
        String pAddr;
        // get into +- if needed
        try {
            pAddr = CbusAddress.CbusPreParseEvent(addr,"T");        
        } catch (IllegalArgumentException e) {
            log.error("48 parse failed with {} ", e.toString());
            throw e;
        }
        
        
        // log.debug("54 after pre parse, pAddr is {} ", pAddr);        
        
        // then, check validity
        try {
            validateSystemNameFormat(pAddr);
        } catch (IllegalArgumentException e) {
            log.error(e.toString());
            throw e;
        }
        

        // log.debug("58 about to create pAddr which should lead with + or - in front {} ", pAddr);
        Turnout t = new CbusTurnout(getSystemPrefix(), pAddr, memo.getTrafficController());
        t.setUserName(userName);
        return t;
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return false;
    }

    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException {
                        // prefix + as service to user
                        
        log.warn("83 about to create systemName {} with Prefix {} ", curAddress, prefix);          
                        
                        
        int unsigned = 0;
        try {
            unsigned = Integer.valueOf(curAddress); // on unsigned integer, will add "+" next
        } catch (NumberFormatException ex) {
            // already warned
        }
        if (unsigned > 0 && !curAddress.startsWith("+")) {
            curAddress = "+" + curAddress;
        }
        
        // first, check validity
        try {
            validateSystemNameFormat(curAddress);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
        }
        return getSystemPrefix() + typeLetter() + curAddress;
    }

    @Override
    public String getNextValidAddress(String curAddress, String prefix) {
        
        // always return this (the current) name without change
        
        // try {
        //    validateSystemNameFormat(curAddress);
        // } catch (IllegalArgumentException e) {
        //    throw new JmriException(e.toString());
        // }
        
        return curAddress;
    }

    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        
        // log.debug ("120 validSystemNameFormat testing for systemName: {} ",systemName );
        String addr = systemName.substring(getSystemPrefix().length() + 1); // get only the address part
        // get into +- if needed
        try {
        addr = CbusAddress.CbusPreParseEvent(addr,"T");        
        } catch (IllegalArgumentException e) {
            log.error(e.toString());
            throw e;
        }
        // log.debug ("139 validSystemNameFormat testing for addr: {} ",addr );
        try {
            validateSystemNameFormat(addr);
        } catch (IllegalArgumentException e){
        log.warn("124 Invalid validSystemNameFormat for: {} Warning: {}",addr ,e.toString());
            return NameValidity.INVALID;
        }
        return NameValidity.VALID;
    }

    /**
     * Work out the details for Cbus hardware address validation
     * Logging of handled cases no higher than WARN.
     *
     * @param address the hardware address to check
     * @throws IllegalArgumentException when delimiter is not found
     */
    void validateSystemNameFormat(String address) throws IllegalArgumentException {
        // log.debug("145 is testing: {} ",address);
        CbusAddress a = new CbusAddress(address);
        // log.debug("147 is new CbusAddress a : {} ",a);
        CbusAddress[] v = a.split();
        if (v == null) {
            throw new IllegalArgumentException("150 null split Did not find usable hardware address: " + address + " for a valid Cbus turnout address");
        }
        switch (v.length) {
            case 1:
                int unsigned = 0;
                try {
                    unsigned = Integer.valueOf(address); // accept unsigned integer, will add "+" upon creation
                } catch (NumberFormatException ex) {
                    // will catch events with node split + hex
                    // log.debug("158 Integer valueof Unable to convert {} into Cbus format +nn", address);
                }
                
                if (address.startsWith("+") || address.startsWith("-") || unsigned > 0) {
                    break;
                }
                throw new IllegalArgumentException("can't make 2nd event from address " + address);
            case 2:
                break;
            default:
                throw new IllegalArgumentException("Wrong number of events in address: " + address);
        }
    }

    @Override
    public String getEntryToolTip() {
        String entryToolTip = Bundle.getMessage("AddOutputEntryToolTip");
        return entryToolTip;
    }

    private final static Logger log = LoggerFactory.getLogger(CbusTurnoutManager.class);

}
