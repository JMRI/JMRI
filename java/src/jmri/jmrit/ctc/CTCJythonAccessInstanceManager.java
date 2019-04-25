package jmri.jmrit.ctc;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public final class CTCJythonAccessInstanceManager {
     public static CTCMain _mCTCMain = new CTCMain();
     public static void reloadFile() { _mCTCMain.rereadXMLFile(); }
     public static boolean _mCTCDebug_TrafficLockingRuleTriggeredDisplayLoggingEnabled = false;
}
