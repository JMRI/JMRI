// IdentifyDecoder.java

package jmri.jmrit.decoderdefn;

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
 * @version			$Id: IdentifyDecoder.java,v 1.3 2001-11-12 21:53:27 jacobsen Exp $
 * @see             jmri.jmrit.roster.RosterEntry
 */
public class IdentifyDecoder extends jmri.jmrit.AbstractIdentify {

	int mfgID = -1; 	// cv8
	int modelID = -1;	// cv7
	
	// steps of the identification state machine
	public boolean test1() {
		// read cv8
		readCV(8);
		return false;
	}
	
	public boolean test2(int value) {
		mfgID = value;
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
		if (s.equals("Done")) done(mfgID, modelID);
		else if (log.isInfoEnabled()) log.info("received status: "+s);
	}
	
	protected void done(int mfgID, int modelID) {
		log.error("Identify decoder done() should have been overridden");
	}
	
	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(IdentifyDecoder.class.getName());
		
}
