// SerialTurnout.java

package jmri.jmrix.cmri.serial;

import jmri.AbstractTurnout;
import jmri.Turnout;

/**
 * SerialTurnout.java
 *
 *  This object doesn't listen to the C/MRI communications.  This is because
 *  it should be the only object that is sending messages for this turnout;
 *  more than one Turnout object pointing to a single device is not allowed.
 *
 *  Turnouts may be controlled by one or two output bits.  If a turnout is 
 *  controlled by two output bits, the output bits must be on the same node,
 *  the address must point to the first output bit, and the second output bit
 *  must follow the output bit in the address.  Valid states for the two bits
 *  controlling the two-bit turnout are:  ON OFF, and OFF ON for the two bits.
 *
 * Description:		extend jmri.AbstractTurnout for C/MRI serial layouts
 * @author			Bob Jacobsen Copyright (C) 2003
 * @version			$Revision: 1.10 $
 */
public class SerialTurnout extends AbstractTurnout {

    /**
     * Create a Turnout object, with both system and user names.
     * <P>
     * 'systemName' was previously validated in SerialTurnoutManager
     */
    public SerialTurnout(String systemName, String userName) {
        super(systemName, userName);
        // Save system Name
        tSystemName = systemName;
        // Extract the Bit from the name
        tBit = SerialAddress.getBitFromSystemName(systemName);
    }

    /**
     * Handle a request to change state by sending a turnout command
     */
    protected void forwardCommandChangeToLayout(int s) {
        // implementing classes will typically have a function/listener to get
        // updates from the layout, which will then call
        //		public void firePropertyChange(String propertyName,
        //				                Object oldValue,
        //						Object newValue)
        // _once_ if anything has changed state (or set the commanded state directly)

        // sort out states
        if ( (s & Turnout.CLOSED) > 0) {
            // first look for the double case, which we can't handle
            if ( (s & Turnout.THROWN) > 0) {
                // this is the disaster case!
                log.error("Cannot command both CLOSED and THROWN "+s);
                return;
            } else {
                // send a CLOSED command
                sendMessage(true^getInverted());
            }
        } else {
            // send a THROWN command
            sendMessage(false^getInverted());
        }
    }

    public void dispose() {}  // no connections need to be broken

    // data members
    String tSystemName; // System Name of this turnout
    protected int tBit;   // bit number of turnout control in Serial node
	protected SerialNode tNode = null;
	protected javax.swing.Timer mPulseClosedTimer = null;
	protected javax.swing.Timer mPulseThrownTimer = null;
	protected boolean mPulseTimerOn = false;

    protected void sendMessage(boolean closed) {
		// if a Pulse Timer is running, ignore the call
		if (!mPulseTimerOn) {
			if (tNode == null){
				tNode = SerialAddress.getNodeFromSystemName(tSystemName);
				if (tNode == null) {
					// node does not exist, ignore call
					return;
				}
			}
			if (getNumberOutputBits() == 1) {
				// check for pulsed control
				if (getControlType() == 0) {
					// steady state control, get current status of the output bit
					if (tNode.getOutputBit(tBit) != closed) {
						// bit state is different from the requested state, set it
						tNode.setOutputBit(tBit, closed);
					}
					else {
						// bit state is the same as requested state, so no change
						//    will occur if requested state is set.
						// check if turnout known state is different from requested state
						int kState = getKnownState();
						if (closed) {
							// CLOSED is being requested
							if ( (kState & Turnout.THROWN) > 0) {
								// known state is different from output bit, set output bit to be correct
								//     for known state, then start a timer to set it to requested state
								tNode.setOutputBit(tBit, false);
								// start a timer to finish setting this turnout
								if (mPulseClosedTimer==null) {
									mPulseClosedTimer = new javax.swing.Timer(1000, new 
											java.awt.event.ActionListener() {
										public void actionPerformed(java.awt.event.ActionEvent e) {
											tNode.setOutputBit(tBit, true);
											mPulseClosedTimer.stop();
											mPulseTimerOn = false;
										}
									});
								}
								mPulseTimerOn = true;
								mPulseClosedTimer.start();
							}
						}
						else {	
							// THROWN is being requested
							if ( (kState & Turnout.CLOSED) > 0) {
								// known state is different from output bit, set output bit to be correct
								//     for known state, then start a timer to set it to requested state
								tNode.setOutputBit(tBit, true);
								// start a timer to finish setting this turnout
								if (mPulseThrownTimer==null) {
									mPulseThrownTimer = new javax.swing.Timer(1000, new 
											java.awt.event.ActionListener() {
										public void actionPerformed(java.awt.event.ActionEvent e) {
											tNode.setOutputBit(tBit, false);
											mPulseThrownTimer.stop();
											mPulseTimerOn = false;								
										}
									});
								}
								mPulseTimerOn = true;
								mPulseThrownTimer.start();
							}						
						}
					}
				}
				else {
					// Pulse control
					int iTime = 1000*getControlType();
					// Get current known state of turnout
					int kState = getKnownState();
					if ( (closed && ((kState & Turnout.THROWN) > 0)) ||
							(!closed && ((kState & Turnout.CLOSED) > 0)) ) {
						// known and requested are different, a change is requested
						//   Pulse the line, first turn bit on
						tNode.setOutputBit(tBit,false);
						// Start a timer to return bit to off state
						if (mPulseClosedTimer==null) {
							mPulseClosedTimer = new javax.swing.Timer(iTime, new 
										java.awt.event.ActionListener() {
								public void actionPerformed(java.awt.event.ActionEvent e) {
									tNode.setOutputBit(tBit, true);
									mPulseClosedTimer.stop();
									mPulseTimerOn = false;
								}
							});
						}
						mPulseTimerOn = true;
						mPulseClosedTimer.start();						
					}
				}	
			} 
			else if (getNumberOutputBits() == 2) {
				// two output bits
				if (getControlType() == 0) {
					// Steady state control e.g. stall motor turnout control
					tNode.setOutputBit(tBit,closed);
					tNode.setOutputBit(tBit+1,!closed);
				}
				else {
					// Pulse control
					int iTime = 1000*getControlType();
					// Get current known state of turnout
					int kState = getKnownState();
					if (closed && ((kState & Turnout.THROWN) > 0)) {
						// CLOSED is requested, currently THROWN - Pulse first bit
						//   Turn bit on
						tNode.setOutputBit(tBit,false);
						// Start a timer to return bit to off state
						if (mPulseClosedTimer==null) {
							mPulseClosedTimer = new javax.swing.Timer(iTime, new 
										java.awt.event.ActionListener() {
								public void actionPerformed(java.awt.event.ActionEvent e) {
									tNode.setOutputBit(tBit, true);
									mPulseClosedTimer.stop();
									mPulseTimerOn = false;
								}
							});
						}
						mPulseTimerOn = true;
						mPulseClosedTimer.start();						
					}
					else if (!closed && ((kState & Turnout.CLOSED) > 0)) {
						// THROWN is requested, currently CLOSED - Pulse second bit
						//   Turn bit on
						tNode.setOutputBit(tBit+1,false);
						// Start a timer to return bit to off state
						if (mPulseThrownTimer==null) {
							mPulseThrownTimer = new javax.swing.Timer(iTime, new 
										java.awt.event.ActionListener() {
								public void actionPerformed(java.awt.event.ActionEvent e) {
									tNode.setOutputBit(tBit+1, true);
									mPulseThrownTimer.stop();
									mPulseTimerOn = false;								
								}
							});
						}
						mPulseTimerOn = true;
						mPulseThrownTimer.start();						
					}
				}
			}
		}
	}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialTurnout.class.getName());
}

/* @(#)SerialTurnout.java */
