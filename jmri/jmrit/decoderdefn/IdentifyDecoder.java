// IdentifyDecoder.java

package jmri.jmrit.decoderdefn;

import jmri.Programmer;
import jmri.InstanceManager;

import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.List;

/**
 * Interact with a programmer to identify the DecoderIndexFile entry for a decoder
 * on the programming track.
 *
 * This is a class (instead of a Roster member function) to simplify use of
 * ProgListener callbacks.
 *
 * Once started, this maintains a List of possible RosterEntrys as
 * it works through the identification progress.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.3 $
 * @see             jmri.jmrit.roster.RosterEntry
 * @see             jmri.jmrit.symbolicprog.CombinedLocoSelPane
 * @see             jmri.jmrit.symbolicprog.NewLocoSelPane
 */
abstract public class IdentifyDecoder extends jmri.jmrit.AbstractIdentify {

	int mfgID = -1; 	// cv8
	int modelID = -1;	// cv7

	// steps of the identification state machine
	public boolean test1() {
        // read cv8
		statusUpdate("Read MFG ID - CV 8");
		readCV(8);
		return false;
	}

	public boolean test2(int value) {
		mfgID = value;
		statusUpdate("Read MFG version - CV 7");
		readCV(7);
		return false;
	}

	public boolean test3(int value) {
		modelID = value;
		return true;
	}

	public boolean test4(int value) {
		log.error("unexpected step 4 reached with value: "+value);
		return true;
	}

	public boolean test5(int value) {
		log.error("unexpected step 5 reached with value: "+value);
		return true;
	}

	public boolean test6(int value) {
		log.error("unexpected step 6 reached with value: "+value);
		return true;
	}

	public boolean test7(int value) {
		log.error("unexpected step 7 reached with value: "+value);
		return true;
	}

	public boolean test8(int value) {
		log.error("unexpected step 8 reached with value: "+value);
		return true;
	}

	protected void statusUpdate(String s) {
		message(s);
		if (s.equals("Done")) done(mfgID, modelID);
		else if (log.isInfoEnabled()) log.info("received status: "+s);
	}

	abstract protected void done(int mfgID, int modelID);

	abstract protected void message(String m);

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(IdentifyDecoder.class.getName());

}
