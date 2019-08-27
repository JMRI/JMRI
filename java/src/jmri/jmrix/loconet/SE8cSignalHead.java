package jmri.jmrix.loconet;

import jmri.implementation.DefaultSignalHead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.SignalHead for signals implemented by an SE8C.
 * <p>
 * This implementation writes out to the physical signal when it's commanded to
 * change appearance, and updates its internal state when it hears commands from
 * other places.
 * <p>
 * To get a complete set of aspects, we assume that the SE8C board has been
 * configured such that the 4th aspect is "dark". We then do flashing aspects by
 * commanding the lit appearance to change.
 * <p>
 * This is a grandfathered implementation that is specific to LocoNet systems. A
 * more general implementation, which can work with any system(s), is available
 * in {@link jmri.implementation.SE8cSignalHead}. This package is maintained so
 * that existing XML files can continue to be read. In particular, it only works
 * with the first LocoNet connection (names LHnnn, not L2Hnnn etc).
 * <p>
 * The algorithms in this class are a collaborative effort of Digitrax, Inc and
 * Bob Jacobsen.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 */
public class SE8cSignalHead extends DefaultSignalHead implements LocoNetListener {

    public SE8cSignalHead(int pNumber, String userName) {
        // create systemname
        super("LH" + pNumber, userName); // NOI18N
        init(pNumber);
    }

    public SE8cSignalHead(int pNumber) {
        // create systemname
        super("LH" + pNumber); // NOI18N
        init(pNumber);
    }

    void init(int pNumber) {
        tc = jmri.InstanceManager.getDefault(LnTrafficController.class);
        mNumber = pNumber;
        mAppearance = DARK;  // start turned off
        // At construction, register for messages
        tc.addLocoNetListener(~0, this);
        updateOutput();
    }

    LnTrafficController tc;

    public int getNumber() {
        return mNumber;
    }

    // Handle a request to change state by sending a LocoNet command
    @Override
    protected void updateOutput() {
        // send SWREQ for close
        LocoNetMessage l = new LocoNetMessage(4);
        l.setOpCode(LnConstants.OPC_SW_REQ);

        int address = 0;
        boolean closed = false;
        if (!mLit) {
            address = mNumber + 1;
            closed = true;
        } else if (!mFlashOn
                && ((mAppearance == FLASHGREEN)
                || (mAppearance == FLASHYELLOW)
                || (mAppearance == FLASHRED))) {
            // flash says to make output dark; 
            // flashing-but-lit is handled below
            address = mNumber + 1;
            closed = true;
        } else {
            // which of the four states?
            switch (mAppearance) {
                case FLASHRED:
                case RED:
                    address = mNumber;
                    closed = false;
                    break;
                case FLASHYELLOW:
                case YELLOW:
                    address = mNumber + 1;
                    closed = false;
                    break;
                case FLASHGREEN:
                case GREEN:
                    address = mNumber;
                    closed = true;
                    break;
                case DARK:
                    address = mNumber + 1;
                    closed = true;
                    break;
                default:
                    log.error("Invalid state request: " + mAppearance);
                    return;
            }
        }
        // compute address fields
        int hiadr = (address - 1) / 128;
        int loadr = (address - 1) - hiadr * 128;
        if (closed) {
            hiadr |= 0x20;
        }

        // set "on" bit
        hiadr |= 0x10;

        // store and send
        l.setElement(1, loadr);
        l.setElement(2, hiadr);
        tc.sendLocoNetMessage(l);
    }

    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //  public void firePropertyChange(String propertyName,
    //      Object oldValue,
    //      Object newValue)
    // _once_ if anything has changed state (or set the commanded state directly)
    @Override
    public void message(LocoNetMessage l) {
        int oldAppearance = mAppearance;
        // parse message type
        switch (l.getOpCode()) {
            case LnConstants.OPC_SW_REQ: {               /* page 9 of LocoNet PE */

                int sw1 = l.getElement(1);
                int sw2 = l.getElement(2);
                if (myAddress(sw1, sw2)) {
                    if ((sw2 & LnConstants.OPC_SW_REQ_DIR) != 0) {
                        // was set CLOSED
                        if (mAppearance != FLASHGREEN) {
                            mAppearance = GREEN;
                        }
                    } else {
                        // was set THROWN
                        if (mAppearance != FLASHRED) {
                            mAppearance = RED;
                        }
                    }
                }
                if (myAddressPlusOne(sw1, sw2)) {
                    if ((sw2 & LnConstants.OPC_SW_REQ_DIR) != 0) {
                        // was set CLOSED, which means DARK
                        // don't change if one of the possibilities already
                        if (!(mAppearance == FLASHYELLOW || mAppearance == DARK
                                || mAppearance == FLASHGREEN || mAppearance == FLASHRED
                                || !mLit || !mFlashOn)) {
                            mAppearance = DARK;    // that's the setting by default
                        }
                    } else {
                        // was set THROWN
                        if (mAppearance != FLASHYELLOW) {
                            mAppearance = YELLOW;
                        }
                    }
                }
                break;
            }
            case LnConstants.OPC_SW_REP: {               /* page 9 of LocoNet PE */

                int sw1 = l.getElement(1);
                int sw2 = l.getElement(2);
                if (myAddress(sw1, sw2)) {
                    // see if its a turnout state report
                    if ((sw2 & LnConstants.OPC_SW_REP_INPUTS) == 0) {
                        // sort out states
                        if ((sw2 & LnConstants.OPC_SW_REP_CLOSED) != 0) {
                            // was set CLOSED
                            if (mAppearance != FLASHGREEN) {
                                mAppearance = GREEN;
                            }
                        }
                        if ((sw2 & LnConstants.OPC_SW_REP_THROWN) != 0) {
                            // was set THROWN
                            if (mAppearance != FLASHRED) {
                                mAppearance = RED;
                            }
                        }
                    }
                }
                if (myAddressPlusOne(sw1, sw2)) {
                    // see if its a turnout state report
                    if ((sw2 & LnConstants.OPC_SW_REP_INPUTS) == 0) {
                        // was set CLOSED, which means DARK
                        // don't change if one of the possibilities already
                        if (!(mAppearance == FLASHYELLOW || mAppearance == DARK
                                || mAppearance == FLASHGREEN || mAppearance == FLASHRED
                                || !mLit || !mFlashOn)) {
                            mAppearance = DARK;    // that's the setting by default
                        }
                    }
                    if ((sw2 & LnConstants.OPC_SW_REP_THROWN) != 0) {
                        // was set THROWN
                        if (mAppearance != FLASHYELLOW) {
                            mAppearance = YELLOW;
                        }
                    }
                }
                return;
            }
            default:
                return;
        }
        // reach here if the state has updated
        if (oldAppearance != mAppearance) {
            firePropertyChange("Appearance", Integer.valueOf(oldAppearance), Integer.valueOf(mAppearance)); // NOI18N
        }
    }

    @Override
    public void dispose() {
        tc.removeLocoNetListener(~0, this);
        super.dispose();
    }

    // data members
    int mNumber;   // LocoNet Turnout number with lower address (0 based)

    private boolean myAddress(int a1, int a2) {
        // the "+ 1" in the following converts to throttle-visible numbering
        return (((a2 & 0x0f) * 128) + (a1 & 0x7f) + 1) == mNumber;
    }

    private boolean myAddressPlusOne(int a1, int a2) {
        // the "+ 1" in the following converts to throttle-visible numbering
        return (((a2 & 0x0f) * 128) + (a1 & 0x7f) + 1) == mNumber + 1;
    }
    private final static Logger log = LoggerFactory.getLogger(SE8cSignalHead.class);

}
