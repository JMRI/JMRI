package jmri.jmrit.ctc.topology;

import java.util.*;
import jmri.*;
import jmri.jmrit.ctc.ctcserialdata.CTCSerialData;

/**
 * This class contains all of the information needed (in lists) for the higher level
 * "TRL_Rules" to generate all of the entries in "_mTRL_TrafficLockingRulesSSVList"
 * 
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019, 2020
 */

public class TopologyInfo {
    private final CTCSerialData _mCTCSerialData;    // Needed to look up a turnout in order to return an O.S. section text.
    private final String _mNormal;                  // Bundle.getMessage("TLE_Normal")
    private final String _mReverse;                 // Bundle.getMessage("TLE_Reverse")
    public TopologyInfo(CTCSerialData CTCSerialData, String normal, String reverse) {
        _mCTCSerialData = CTCSerialData;
        _mNormal = normal;
        _mReverse = reverse;
    }
    /**
     * Simple class to contain simple info about a turnout.
     */
    private static class TurnoutInfo {
        public final String _mOSSectionText;
        public final String _mNormalReversed;
        private TurnoutInfo() { _mOSSectionText = ""; _mNormalReversed = ""; }
        public TurnoutInfo(String OSSectionText, String normalReversed) {
            _mOSSectionText = OSSectionText;
            _mNormalReversed = normalReversed;
        }
    }
    private final LinkedList<Sensor> _mSensors = new LinkedList<>();
//  private final LinkedList<String> _mSensorNamesDebug = new LinkedList<>();    //????
    private final LinkedList<TurnoutInfo> _mTurnoutInfos = new LinkedList<>();
//  private final LinkedList<String> _mOSSectionInfosDebug = new LinkedList<>(); //????
    private final LinkedList<Turnout> _mTurnouts = new LinkedList<>();  // ONLY used for duplicate check (lazy).
    
    /**
     * @return true if any of our lists have anything.
     */
    public boolean nonEmpty() { return !_mSensors.isEmpty() || !_mTurnoutInfos.isEmpty(); }
    
    
    /**    
     * Quick and dirty routine to all all of the sensors in the passed blocks to
     * our internal lists.  Duplicates are ignored.
     * @param blocks    List of Blocks to add.
     */
    public void addBlocks(List<Block> blocks) {
        for (Block block : blocks) {
            Sensor sensor = block.getSensor();
            if (!_mSensors.contains(sensor)) { //  VERIFY not in list already for some reason (safety, shouldn't happen):
                _mSensors.add(sensor);
//              _mSensorNamesDebug.add(sensor.getDisplayName());
            }
        }
    }
    
    
    /**
     * Quick and dirty routine to add all of the turnouts in SML to our internal lists.
     * Duplicates are ignored.
     * 
     * @param signalMastLogic   SML to work against.
     * @param signalMast        Destination mast in SML.
     */
    public void addTurnouts(SignalMastLogic signalMastLogic, SignalMast signalMast) {
//  Right now, I cannot make a subroutine call out of this, because I have to call two different
//  routines at the lowest level: "signalMastLogic.getTurnoutState" and "signalMastLogic.getAutoTurnoutState"
//  depending on which it is.  In Java method reference is a way.  But I'm lazy and in a hurry:
        for (Turnout turnout : signalMastLogic.getTurnouts(signalMast)) {
            if (!_mTurnouts.contains(turnout)) {    // VERIFY not in list already for some reason (safety, shouldn't happen):
                _mTurnouts.add(turnout);            // For above if statement dup check.
//  Need to convert the turnout to an O.S. section text:                
                String OSSectionText = _mCTCSerialData.convertTurnoutToOSSectionDesignation(turnout);
                if (null != OSSectionText) { // Safety:
//  ToDo someday: Reverse "isNormal" if feedback different?                    
                    boolean isNormal = signalMastLogic.getTurnoutState(turnout, signalMast) == Turnout.CLOSED;
                    _mTurnoutInfos.add(new TurnoutInfo(OSSectionText, isNormal ? _mNormal : _mReverse));
//                  _mOSSectionInfosDebug.add(OSSectionText);
                }
            }
        }
        for (Turnout turnout : signalMastLogic.getAutoTurnouts(signalMast)) {
            if (!_mTurnouts.contains(turnout)) {    // VERIFY not in list already for some reason (safety, shouldn't happen):
                _mTurnouts.add(turnout);            // For above if statement dup check.
//  Need to convert the turnout to an O.S. section text:                
                String OSSectionText = _mCTCSerialData.convertTurnoutToOSSectionDesignation(turnout);
                if (null != OSSectionText) { // Safety:
//  ToDo someday: Reverse "isNormal" if feedback different?                    
                    boolean isNormal = signalMastLogic.getAutoTurnoutState(turnout, signalMast) == Turnout.CLOSED;
                    _mTurnoutInfos.add(new TurnoutInfo(OSSectionText, isNormal ? _mNormal : _mReverse));
//                  _mOSSectionInfosDebug.add(OSSectionText);
                }
            }
        }
    }
}
