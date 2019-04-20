package jmri.jmrit.ctc;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public final class CTCJythonAccessInstanceManager {
     public static CTCMain _mCTCMain = null;    // CTCFileModel sets this when it's loaded.
     public static void reloadFile() { if (_mCTCMain != null) { _mCTCMain.rereadXMLFile(); } }  // Safety.
     public static boolean _mCTCDebug_TrafficLockingRuleTriggeredDisplayLoggingEnabled = false;
}
