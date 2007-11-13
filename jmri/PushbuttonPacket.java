// PushbuttonPacket.java

package jmri;

/**
 * Generates an NMRA packet containing the correct payload to enable or
 * disable pushbutton lockout.  Currently supports the following Decoders
 * NCE 
 * CVP AD4
 * 
 * 
 * 
 * NCE is the easliest to implement, CV556 = 0 disable lockout, CV556 = 1 enable lockout
 * 
 * CVP is a bit tricker, CV514 controls the lockout for four turnouts.  Each turnout
 * can have one or two button controls.  Therefore the user must specify if they are
 * using one or two buttons for each turnout.
 * 
 * From the CVP user manual:
 * 
 * Function			CV514
 * Lock all inputs	0
 * Unlock 1			1
 * Unlock 2			4
 * Unlock 3			16
 * Unlock 4			64
 * Unlock all		85
 * Enable 2 button	255
 * 
 * This routine assumes that for two button operations the following table is true:
 * 
 * Lock all inputs	0
 * Unlock 1			3
 * Unlock 2			12
 * Unlock 3			48
 * Unlock 4			192
 * Unlock all		255
 * 
 * Each CVP can operate up to four turnouts, luckly for us, they are sequential. 
 *
 * @author      Daniel Boudreau Copyright (C) 2007
 * @version     $Revision: 1.2 $
 * 
 */
public class PushbuttonPacket {
	
	/**
	 * Valid stationary decoder names
	 */
	public final static String NCEname = "NCE_Rev_C";
	public final static String CVP_1Bname = "CVP_AD4_1B";
	public final static String CVP_2Bname = "CVP_AD4_2B";
	
	protected final static String[] VAILDDECODERNAMES = { NCEname, CVP_1Bname,
		CVP_2Bname };

	public static byte[] pushbuttonPkt(int turnoutNum, boolean locked) {
		
		char sysLetter = InstanceManager.turnoutManagerInstance().systemLetter();
		
		Turnout t = InstanceManager.turnoutManagerInstance().getBySystemName(sysLetter+"T"+ turnoutNum);
		byte[] bl;
		
		if (t.getDecoderName().equals(NCEname)) {
			if (locked)
				bl = NmraPacket.accDecoderPktOpsMode(turnoutNum, 556, 1);
			else
				bl = NmraPacket.accDecoderPktOpsMode(turnoutNum, 556, 0);
			return bl;
			
		// need to add fine control to CVP turnout lockout data
		} else if (t.getDecoderName().equals(CVP_1Bname)
				|| t.getDecoderName().equals(CVP_2Bname)) {
			int CVdata = CVPturnoutLockout(turnoutNum);
			bl = NmraPacket.accDecoderPktOpsMode(turnoutNum, 514, CVdata);
			return bl;
		} else {
			return null;
		}
	}
	
	public static String[] getValidDecoderNames() {
		return VAILDDECODERNAMES;
	}
	
	// builds the data byte for CVP decoders, builds based on JMRI's current
	// knowledge of turnout pushbutton lockout states. If a turnout doesn't
	// exist, assume single button operation.
	private static int CVPturnoutLockout(int turnoutNum) {

		char sysLetter = InstanceManager.turnoutManagerInstance().systemLetter();
		int CVdata = 0;
		int oneButton = 1;
		int twoButton = 3;
		int modTurnoutNum = (turnoutNum-1) & 0xFFC; // mask off bits, there are 4 turnouts per
													// controller

		for (int i = 0; i < 4; i++) {
			// set the default for one button in case the turnout doesn't exist
			int button = oneButton;
			modTurnoutNum++;
			Turnout t = InstanceManager.turnoutManagerInstance()
					.getBySystemName(sysLetter + "T" + modTurnoutNum);
			if (t != null) {
				if (t.getDecoderName().equals(CVP_1Bname)) {
					// do nothing button already = oneButton
				} else if (t.getDecoderName().equals(CVP_2Bname)) {
					button = twoButton;
				} else {
					log.warn("Turnout " + modTurnoutNum
							+ ", all CVP turnouts on one decoder should be "
							+ CVP_1Bname + " or " + CVP_2Bname);
				}
				// zero out the bits if the turnout is locked
				if (t.getLocked(Turnout.PUSHBUTTONLOCKOUT)) {
					button = 0;
				}
			}
			CVdata = CVdata + button;
			oneButton = oneButton << 2; // move to the next turnout
			twoButton = twoButton << 2;

		}
		return CVdata;
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(PushbuttonPacket.class.getName());
}


/* @(#)NmraPacket.java */

