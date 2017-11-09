package jmri.jmrix.loconet;

import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the LocoNet-specific Turnout implementation.
 * System names are "LTnnn", where nnn is the turnout number without padding.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 * <P>
 * Since LocoNet messages requesting turnout operations can arrive faster than
 * the command station can send them on the rails, the command station has a
 * short queue of messages. When that gets full, it sends a LACK, indicating
 * that the request was not forwarded on the rails. In that case, this class
 * goes into a tight loop, resending the last turnout message seen until it's
 * received without a LACK reply. Note two things about this:
 * <UL>
 * <LI>We provide this service for any turnout request, whether or not it came
 * from JMRI. (This might be a problem if more than one computer is executing
 * this algorithm)
 * <LI>By sending the message as fast as we can, we tie up the LocoNet during
 * the the recovery. This is a mixed bag; delaying can cause messages to get out
 * of sequence on the rails. But not delaying takes up a lot of LocoNet
 * bandwidth.
 * </UL>
 * In the end, this implementation is OK, but not great. An improvement would be
 * to control JMRI turnout operations centrally, so that retransmissions can be
 * controlled.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2007
 */
public class LnTurnoutManager extends jmri.managers.AbstractTurnoutManager implements LocoNetListener {

    // ctor has to register for LocoNet events
    public LnTurnoutManager(LocoNetInterface fastcontroller, LocoNetInterface throttledcontroller, String prefix, boolean mTurnoutNoRetry) {
        this.fastcontroller = fastcontroller;
        this.throttledcontroller = throttledcontroller;
        this.prefix = prefix;
        this.mTurnoutNoRetry = mTurnoutNoRetry;

        if (fastcontroller != null) {
            fastcontroller.addLocoNetListener(~0, this);
        } else {
            log.error("No layout connection, turnout manager can't function");
        }
    }

    LocoNetInterface fastcontroller;
    LocoNetInterface throttledcontroller;
    boolean mTurnoutNoRetry;
    private String prefix;

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    @Override
    public void dispose() {
        if (fastcontroller != null) {
            fastcontroller.removeLocoNetListener(~0, this);
        }
        super.dispose();
    }

    protected boolean _binaryOutput = false;
    protected boolean _useOffSwReqAsConfirmation = false;

    public void setUhlenbrockMonitoring() {
        _binaryOutput = true;
        mTurnoutNoRetry = true;
        _useOffSwReqAsConfirmation = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Turnout createNewTurnout(String systemName, String userName) throws IllegalArgumentException {
        int addr;
        try {
            addr = Integer.valueOf(systemName.substring(prefix.length() + 1)).intValue();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Can't convert " +  // NOI18N
                    systemName.substring(prefix.length() + 1) +
                    " to LocoNet turnout address"); // NOI18N
        }
        LnTurnout t = new LnTurnout(getSystemPrefix(), addr, throttledcontroller);
        t.setUserName(userName);
        if (_binaryOutput) t.setBinaryOutput(true);
        if (_useOffSwReqAsConfirmation) {
            t.setUseOffSwReqAsConfirmation(true);
            t.setFeedbackMode("MONITORING"); // NOI18N
        }
        return t;
    }

    // holds last seen turnout request for possible resend
    LocoNetMessage lastSWREQ = null;

    /**
     * Listen for turnouts, creating them as needed
     */
    @Override
    public void message(LocoNetMessage l) {
        log.debug("LnTurnoutManager message {}", l);
        // parse message type
        int addr;
        switch (l.getOpCode()) {
            case LnConstants.OPC_SW_REQ: {               /* page 9 of Loconet PE */

                int sw1 = l.getElement(1);
                int sw2 = l.getElement(2);
                addr = address(sw1, sw2);

                // store message in case resend is needed
                lastSWREQ = l;

                // Loconet spec says 0x10 of SW2 must be 1, but we observe 0
                if (((sw1 & 0xFC) == 0x78) && ((sw2 & 0xCF) == 0x07)) {
                    return;  // turnout interrogate msg
                }
                log.debug("SW_REQ received with address {}", addr);
                break;
            }
            case LnConstants.OPC_SW_REP: {                /* page 9 of Loconet PE */

                // clear resend message, indicating not to resend

                lastSWREQ = null;

                // process this request
                int sw1 = l.getElement(1);
                int sw2 = l.getElement(2);
                addr = address(sw1, sw2);
                log.debug("SW_REP received with address {}", addr);
                break;
            }
            case LnConstants.OPC_LONG_ACK: {
                // might have to resend, check 2nd byte
                if (lastSWREQ != null && l.getElement(1) == 0x30 && l.getElement(2) == 0 && !mTurnoutNoRetry) {
                    // received LONG_ACK reject msg, resend
                    fastcontroller.sendLocoNetMessage(lastSWREQ);
                }

                // clear so can't resend recursively (we'll see
                // the resent message echo'd back)
                lastSWREQ = null;
                return;
            }
            default:  // here we didn't find an interesting command
                // clear resend message, indicating not to resend
                lastSWREQ = null;
                return;
        }
        // reach here for loconet switch command; make sure we know about this one
        String s = prefix + "T" + addr; // NOI18N
        if (getBySystemName(s) == null) {
            // no turnout with this address, is there a light?
            String sx = prefix + "L" + addr; // NOI18N
            if (jmri.InstanceManager.lightManagerInstance().getBySystemName(sx) == null) {
                // no light, create a turnout
                LnTurnout t = (LnTurnout) provideTurnout(s);
                t.message(l);
            }
        }
    }

    private int address(int a1, int a2) {
        // the "+ 1" in the following converts to throttle-visible numbering
        return (((a2 & 0x0f) * 128) + (a1 & 0x7f) + 1);
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    /**
     * Public method to validate system name format.
     *
     * @return 'true' if system name has a valid format, else returns 'false'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (getBitFromSystemName(systemName) != 0) ? NameValidity.VALID : NameValidity.INVALID;
    }

    /**
     * Get the bit address from the system name.
     */
    public int getBitFromSystemName(String systemName) {
        // validate the system Name leader characters
        if (!systemName.startsWith(prefix + "T")) {
            // here if an illegal loconet turnout system name
            log.error("invalid character in header field of loconet turnout system name: {}", systemName);
            return (0);
        }
        // name must be in the LTnnnnn format (L is user configurable)
        int num = 0;
        try {
            num = Integer.valueOf(systemName.substring(
                    prefix.length() + 1, systemName.length())
            ).intValue();
        } catch (Exception e) {
            log.debug("invalid character in number field of system name: {}", systemName);
            return (0);
        }
        if (num <= 0) {
            log.debug("invalid loconet turnout system name: {}", systemName);
            return (0);
        } else if (num > 4096) {
            log.debug("bit number out of range in loconet turnout system name: {}", systemName);
            return (0);
        }
        return (num);
    }

    /**
     * Provide a manager-specific tooltip for the Add new item beantable pane.
     */
    @Override
    public String getEntryToolTip() {
        String entryToolTip = Bundle.getMessage("AddOutputEntryToolTip");
        return entryToolTip;
    }

    private final static Logger log = LoggerFactory.getLogger(LnTurnoutManager.class);

}
