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
 * @author    Bob Jacobsen   Copyright (C) 2001
 * @author    Howard G. Penny   Copyright (C) 2005
 * @version   $Revision: 1.6 $
 * @see       jmri.jmrit.roster.RosterEntry
 * @see       jmri.jmrit.symbolicprog.CombinedLocoSelPane
 * @see       jmri.jmrit.symbolicprog.NewLocoSelPane
 */
abstract public class IdentifyDecoder extends jmri.jmrit.AbstractIdentify {

    int mfgID = -1; 	// cv8
    int modelID = -1;	// cv7
    int productIDhigh = -1;
    int productIDlow  = -1;
    int productID  = -1;

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
        if (mfgID == 113) {
            statusUpdate("Set PI for Read Product ID High Byte");
            writeCV(49, 254);
            return false;
        }
        return true;
    }

    public boolean test4(int value) {
        if (mfgID == 113) {
            statusUpdate("Set SI for Read Product ID High Byte");
            writeCV(50, 4);
            return false;
        }
        log.error("unexpected step 4 reached with value: "+value);
        return true;
    }

    public boolean test5(int value) {
        if (mfgID == 113) {
            statusUpdate("Read Product ID High Byte");
            readCV(56);
            return false;
        }
        log.error("unexpected step 5 reached with value: "+value);
        return true;
    }

    public boolean test6(int value) {
        if (mfgID == 113) {
            productIDhigh = value;
            statusUpdate("Set SI for Read Product ID Low Byte");
            writeCV(50, 5);
            return false;
        }
        log.error("unexpected step 6 reached with value: "+value);
        return true;
    }

    public boolean test7(int value) {
        if (mfgID == 113) {
            statusUpdate("Read Product ID Low Byte");
            readCV(56);
            return false;
        }
        log.error("unexpected step 7 reached with value: "+value);
        return true;
    }

    public boolean test8(int value) {
        if (mfgID == 113) {
            productIDlow = value;
            productID = (productIDhigh * 256) + productIDlow;
            return true;
        }
        log.error("unexpected step 8 reached with value: "+value);
        return true;
    }

    protected void statusUpdate(String s) {
        message(s);
        if (s.equals("Done")) done(mfgID, modelID, productID);
        else if (log.isDebugEnabled()) log.debug("received status: "+s);
    }

    abstract protected void done(int mfgID, int modelID, int productID);

    abstract protected void message(String m);

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(IdentifyDecoder.class.getName());

}
