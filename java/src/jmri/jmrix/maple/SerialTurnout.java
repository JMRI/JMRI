package jmri.jmrix.maple;

import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Turnout implementation for Maple systems.
 * <p>
 * This object doesn't listen to the interface communications. This is because
 * it should be the only object that is sending messages for this turnout; more
 * than one Turnout object pointing to a single device is not allowed.
 * <p>
 * Turnouts on the layout may be controlled by one or two output bits. To
 * control a turnout from one Turnout object via two output bits, the output
 * bits must be on the same node, the Turnout address must point to the first
 * output bit, and the second output bit must follow the output bit at the next
 * address. Valid states for the two bits controlling the two-bit turnout are:
 * ON OFF, and OFF ON for the two bits.
 * <p>
 * This class can also drive pulsed outputs, which can be combined with the
 * two-bit option in the expected ways.
 * <p>
 * When a Turnout is configured for pulsed and two-output, a request to go to a
 * new CommandedState sets the desired configuration for the pulse interval,
 * then sets both leads to their off condition.
 * <p>
 * When a Turnout is configured for pulsed and one output, a request to go to a
 * new CommandedState just sets the output on for the interval; it's assumed
 * that there's something out on the layout that converts that pulse into a
 * "flip to other state" operation.
 * <p>
 * Finally, this implementation supports the "inverted" option. Inverted applies
 * to the status of the lead on the output itself.
 * <p>
 * For example, a pulsed, two-output, inverted turnout will have both pins set
 * to 1 in the resting state. When THROWN, one lead will be set to 0 for the
 * configured interval, then set back to 1.
 * <p>
 * For more discussion of this, please see the
 * <a href="http://jmri.org/help/en/html/hardware/cmri/CMRI.shtml#options">documentation
 * page</a>
 * (for C/MRI, but this is similar).
 *
 * NOTE: In the current version Maple support, code for implementing pulsed
 * turnouts has been commented out.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2007, 2008
 * @author David Duchamp Copyright (C) 2004, 2007
 * @author Dan Boudreau Copyright (C) 2007
  */
public class SerialTurnout extends AbstractTurnout {

    private MapleSystemConnectionMemo _memo = null;

    /**
     * Create a Turnout object, with both system and user names.
     * <p>
     * 'systemName' has already been validated in SerialTurnoutManager
     *
     * @param systemName the system name for this Turnout
     * @param userName   the user name for this Turnout
     * @param memo       the memo for the system connection
     */
    public SerialTurnout(String systemName, String userName, MapleSystemConnectionMemo memo) {
        super(systemName, userName);
        // Save system Name
        tSystemName = systemName;
        _memo = memo;
        // Extract the Bit from the name
        tBit = SerialAddress.getBitFromSystemName(systemName, _memo.getSystemPrefix());
    }

    /**
     * Create a Turnout object, with only a system name.
     * <p>
     * 'systemName' has already been validated in SerialTurnoutManager
     *
     * @param systemName the system name for this Turnout
     * @param memo       the memo for the system connection
     */
    public SerialTurnout(String systemName, MapleSystemConnectionMemo memo) {
        super(systemName);
        // Save system Name
        tSystemName = systemName;
        _memo = memo;
        // Extract the Bit from the name
        tBit = SerialAddress.getBitFromSystemName(systemName, _memo.getSystemPrefix());
    }

    /**
     * Handle a request to change state by sending a turnout command
     *
     * @param newState desired new state, one of the Turnout class constants
     */
    @Override
    protected void forwardCommandChangeToLayout(int newState) {
        // implementing classes will typically have a function/listener to get
        // updates from the layout, which will then call
        //  public void firePropertyChange(String propertyName,
        //                    Object oldValue,
        //      Object newValue)
        // _once_ if anything has changed state (or set the commanded state directly)

        // sort out states
        if ((newState & Turnout.CLOSED) != 0) {
            // first look for the double case, which we can't handle
            if ((newState & Turnout.THROWN) != 0) {
                // this is the disaster case!
                log.error("Cannot command both CLOSED and THROWN: {}", newState);
                return;
            } else {
                // send a CLOSED command
                sendMessage(true);
            }
        } else {
            // send a THROWN command
            sendMessage(false);
        }
    }

    /**
     * Turnouts do support inversion
     */
    @Override
    public boolean canInvert() {
        return true;
    }

    @Override
    protected void turnoutPushbuttonLockout(boolean _pushButtonLockout) {
        if (log.isDebugEnabled()) {
            log.debug("Send command to {} Pushbutton", (_pushButtonLockout ? "Lock" : "Unlock"));
        }
    }

    // data members
    String tSystemName; // System Name of this turnout
    protected int tBit;   // bit number of turnout control in Serial node
// protected SerialNode tNode = null;
    protected javax.swing.Timer mPulseClosedTimer = null;
    protected javax.swing.Timer mPulseThrownTimer = null;
    protected boolean mPulseTimerOn = false;

    /**
     * Control the actual layout hardware. The request is for a particular
     * functional setting, e.g. CLOSED or THROWN. The "inverted" status of the
     * output leads is handled here.
     */
    protected void sendMessage(boolean closed) {
        // if a Pulse Timer is running, ignore the call
        if (!mPulseTimerOn) {
            if (getNumberOutputBits() == 1) {
                // check for pulsed control
                if (getControlType() == 0) {
                    // steady state control, get current status of the output bit
                    if ((_memo.getTrafficController().outputBits().getOutputBit(tBit) ^ getInverted()) != closed) {
                        // bit state is different from the requested state, set it
                        _memo.getTrafficController().outputBits().setOutputBit(tBit, closed ^ getInverted());
                    } else {
                        // Bit state is the same as requested state, so nothing
                        // will happen if requested state is set.
                        // Check if turnout known state is different from requested state
                        int kState = getKnownState();
                        if (closed) {
                            // CLOSED is being requested
                            if ((kState & Turnout.THROWN) != 0) {
                                // known state is different from output bit, set output bit to be correct
                                //     for known state, then start a timer to set it to requested state
                                _memo.getTrafficController().outputBits().setOutputBit(tBit, false ^ getInverted());
//        // start a timer to finish setting this turnout
//        if (mPulseClosedTimer==null) {
//         mPulseClosedTimer = new javax.swing.Timer(OutputBits.instance().getPulseWidth(), 
//           new java.awt.event.ActionListener() {
//          public void actionPerformed(java.awt.event.ActionEvent e) {
//           OutputBits.instance().setOutputBit(tBit, true^getInverted());
//           mPulseClosedTimer.stop();
//           mPulseTimerOn = false;
//          }
//         });
//        }
//        mPulseTimerOn = true;
//        mPulseClosedTimer.start();
                            }
                        } else {
                            // THROWN is being requested
                            if ((kState & Turnout.CLOSED) != 0) {
                                // known state is different from output bit, set output bit to be correct
                                //     for known state, then start a timer to set it to requested state
                                _memo.getTrafficController().outputBits().setOutputBit(tBit, true ^ getInverted());
//        // start a timer to finish setting this turnout
//        if (mPulseThrownTimer==null) {
//         mPulseThrownTimer = new javax.swing.Timer(OutputBits.instance().getPulseWidth(), 
//           new java.awt.event.ActionListener() {
//          public void actionPerformed(java.awt.event.ActionEvent e) {
//           OutputBits.instance().setOutputBit(tBit, false^getInverted());
//           mPulseThrownTimer.stop();
//           mPulseTimerOn = false;        
//          }
//         });
//        }
//        mPulseTimerOn = true;
//        mPulseThrownTimer.start();
                            }
                        }
                    }
                } else {
                    // Pulse control
//     int iTime = OutputBits.instance().getPulseWidth();
//     // Get current known state of turnout
//     int kState = getKnownState();
//     if ( (closed && ((kState & Turnout.THROWN) != 0)) ||
//       (!closed && ((kState & Turnout.CLOSED) != 0)) ) {
//      // known and requested are different, a change is requested
//      //   Pulse the line, first turn bit on
//      OutputBits.instance().setOutputBit(tBit,false^getInverted());
//      // Start a timer to return bit to off state
//      if (mPulseClosedTimer==null) {
//       mPulseClosedTimer = new javax.swing.Timer(iTime, new 
//          java.awt.event.ActionListener() {
//        public void actionPerformed(java.awt.event.ActionEvent e) {
//         OutputBits.instance().setOutputBit(tBit, true^getInverted());
//         mPulseClosedTimer.stop();
//         mPulseTimerOn = false;
//        }
//       });
//      }
//      mPulseTimerOn = true;
//      mPulseClosedTimer.start();      
//     }
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTurnout.class);

}
