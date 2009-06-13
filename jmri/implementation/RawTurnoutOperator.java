/**
 * Concrete subclass of TurnoutOperator for a turnout that has no feedback.
 * This operator sends raw NMRA accessory decoder packets to the layout 
 * instead of using the built in turnout code.  It should be used only with
 * turnouts with DIRECT, ONESENSOR or TWOSENSOR feedback.
 * This class is based on the NoFeedbackTurnoutOperator class.
 * 
 * @author	Paul Bender	Copyright 2008
 */
package jmri.implementation;

import jmri.*;

public class RawTurnoutOperator extends TurnoutOperator {

	long interval;
	int maxTries;
	int tries = 0;
        int address = 0;
	
	public RawTurnoutOperator(AbstractTurnout t, long i, int mt) {
		super(t);
                address=Integer.parseInt(t.getSystemName().substring(2));
		interval = i;
		maxTries = mt;
	}

        private void sendCommand(){
            byte pkt[]=jmri.NmraPacket.accDecoderPkt(address,myTurnout.getCommandedState()==Turnout.CLOSED);
            jmri.InstanceManager.commandStationInstance().sendPacket(pkt,1);
        }
	
	/**
	 * Do the autmation for a turnout with no feedback. This means try
	 * maxTries times at an interval of interval. Note the call to
	 * operatorCheck each time we're about to actually do something -
	 * if we're no longer the current operator this throws
	 * TurnoutOperatorException which just terminates the thread.
	 */
	public void run() {
		try {
			operatorCheck();
                        sendCommand();
			while (++tries < maxTries) {
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
				    Thread.currentThread().interrupt(); // retain if needed later
				}
				operatorCheck();
                sendCommand();
			}
			myTurnout.setKnownStateToCommanded();
		} catch (TurnoutOperatorException e) { }
	}

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RawTurnoutOperator.class.getName());
}
