package jmri.jmrix.acela;

import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Turnout Object for Acela
 * <p>
 * Based in part on SerialTurnout.java
 *
 * @author Dave Duchamp Copyright (C) 2004
 *
 * @author Bob Coleman Copyright (C) 2007, 2008 Based on CMRI serial example,
 * modified to establish Acela support.
 */
public class AcelaTurnout extends AbstractTurnout {

    private AcelaSystemConnectionMemo _memo = null;

    /**
     * Create a Turnout object, with only system name.
     * <p>
     * 'SystemName' was previously validated in AcelaTurnoutManager
     *
     * @param systemName the system name for this Turnout
     * @param memo       the memo for the system connection
     */
    public AcelaTurnout(String systemName, AcelaSystemConnectionMemo memo) {
        super(systemName);
        _memo = memo;
        initializeTurnout(systemName);
    }

    /**
     * Create a Turnout object, with both system and user names.
     * <p>
     * 'systemName' was previously validated in AcelaTurnoutManager
     *
     * @param systemName the system name for this Turnout
     * @param userName   the user name for this Turnout
     * @param memo       the memo for the system connection
     */
    public AcelaTurnout(String systemName, String userName, AcelaSystemConnectionMemo memo) {
        super(systemName, userName);
        _memo = memo;
        initializeTurnout(systemName);
    }

    /**
     * Set up system dependent instance variables and set system independent
     * instance variables to default values.
     * Note: most instance variables are in AbstractTurnout.java
     */
    private void initializeTurnout(String systemName) {
        // Extract the Bit from the name
        mBit = AcelaAddress.getBitFromSystemName(systemName, _memo.getSystemPrefix());

        // Set initial state
        setState(UNKNOWN);
        // Set defaults for all other instance variables
    }

    /**
     * System dependent instance variables
     */
    protected int mState = UNKNOWN;  // current state of this turnout
    int mBit = -1;                // global address from 0

    /**
     * {@inheritDoc}
     */
    @Override
    protected void forwardCommandChangeToLayout(int newState) {
        try {
            sendMessage(stateChangeCheck(newState));
        } catch (IllegalArgumentException ex) {
            log.error("new state invalid, Turnout not set");
        }
    }

    /**
     * Send a message to the layout to lock or unlock the turnout push buttons.
     * <p>
     * This implementation does nothing, as Acela turnouts do not support
     * lockout.
     *
     * @param pushButtonLockout true to lockout turnout push buttons; false
     *                          otherwise
     */
    @Override
    protected void turnoutPushbuttonLockout(boolean pushButtonLockout) {
        // Acela turnouts do not currently support lockout
    }

    // Acela turnouts do support inversion
    @Override
    public boolean canInvert() {
        return true;
    }

    // method which takes a turnout state as a parameter and adjusts it as necessary
    // to reflect the turnout invert property
    private int adjustStateForInversion(int rawState) {
        if (getInverted() && (rawState == CLOSED || rawState == THROWN)) {
            if (rawState == CLOSED) {
                return THROWN;
            } else {
                return CLOSED;
            }
        } else {
            return rawState;
        }
    }

    protected void sendMessage(boolean closed) {
        int newState;
        if (closed) {
            newState = adjustStateForInversion(CLOSED);
        } else {
            newState = adjustStateForInversion(THROWN);
        }

        AcelaNode mNode = AcelaAddress.getNodeFromSystemName(mSystemName, _memo);

        if (mNode != null) {
            switch (newState) {
                case THROWN:
                    mNode.setOutputBit(mBit, true);
                    break;
                case CLOSED:
                    mNode.setOutputBit(mBit, false);
                    break;
                default:
                    log.warn("illegal state requested for Turnout: {}", getSystemName());
                    break;
            }
        }

        if (newState != mState) {
            int oldState = mState;
            mState = newState;

            // notify listeners, if any
            firePropertyChange("KnownState", oldState, newState);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaTurnout.class);

}
