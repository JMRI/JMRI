package jmri.jmrit.ctc;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.SensorManager;
import jmri.SignalHeadManager;
import jmri.SignalMastManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctcserialdata.ProjectsCommonSubs;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 * 
 * Simple object to provide support for CTCEditor queries about the validity
 * of different objects
 */
public class SimpleValidationServer {
    private final DataOutputStream _mDataOutputStream;
    private static final String INVALID_SEQUENCE_NUMBER = "X";  // In case user doesn't give us what we need to give back to them regarding sequence numbers!

    private static final SensorManager SENSOR_MANAGER = InstanceManager.sensorManagerInstance();
    private static final TurnoutManager TURNOUT_MANAGER = InstanceManager.turnoutManagerInstance(); //????
    private static final SignalHeadManager SIGNAL_HEAD_MANAGER = InstanceManager.getDefault(jmri.SignalHeadManager.class);
    private static final SignalMastManager SIGNAL_MAST_MANAGER = InstanceManager.getDefault(jmri.SignalMastManager.class);
    private static final BlockManager BLOCK_MANAGER = InstanceManager.getDefault(BlockManager.class);
    
    public SimpleValidationServer(DataOutputStream dataOutputStream) {
        _mDataOutputStream = dataOutputStream;
    }
    
//  private ArrayList<String> rcv = new ArrayList<>();  //!!!!
//  private ArrayList<String> send = new ArrayList<>(); //!!!!
    
    public void parseStatus(String statusString) /*throws jmri.JmriException*/ {
//      rcv.add(statusString);
        if (ProjectsCommonSubs.isNullOrEmptyString(statusString)) { sendNo(INVALID_SEQUENCE_NUMBER); return; }
        String[] args = statusString.split(" ");
        String sequenceNumber = args.length >= 2 ? args[1] : INVALID_SEQUENCE_NUMBER;
        if (args.length < 4) { sendNo(sequenceNumber); return; }
        String objectTypeToCheck = args[2];
        String jmriObjectToCheck = args[3];
        if (objectTypeToCheck.equals(CodeButtonHandlerData.objectTypeToCheck.KEEP_ALIVE.toString())) {
            sendYes(sequenceNumber); return;
        } else if (objectTypeToCheck.equals(CodeButtonHandlerData.objectTypeToCheck.SENSOR.toString())) {
            if (SENSOR_MANAGER.getSensor(jmriObjectToCheck) != null) { sendYes(sequenceNumber); return; }
        } else if (objectTypeToCheck.equals(CodeButtonHandlerData.objectTypeToCheck.TURNOUT.toString())) {
            if (TURNOUT_MANAGER.getTurnout(jmriObjectToCheck) != null) { sendYes(sequenceNumber); return; }
        } else if (objectTypeToCheck.equals(CodeButtonHandlerData.objectTypeToCheck.SIGNAL.toString())) {
            if (SIGNAL_HEAD_MANAGER.getSignalHead(jmriObjectToCheck) != null) { sendYes(sequenceNumber); return; }  // Try BOTH:
            if (SIGNAL_MAST_MANAGER.getSignalMast(jmriObjectToCheck) != null) { sendYes(sequenceNumber); return; }
        } else if (objectTypeToCheck.equals(CodeButtonHandlerData.objectTypeToCheck.BLOCK.toString())) {
            if (BLOCK_MANAGER.getBlock(jmriObjectToCheck) != null) { sendYes(sequenceNumber); return; }
        }
        sendNo(sequenceNumber);
    }
   
    private void sendYes(String sequenceNumber) { try { _mDataOutputStream.writeBytes(sequenceNumber + " YES\n"); /*send.add(sequenceNumber + " YES");*/ } catch (IOException ex) {} }
    private void sendNo(String sequenceNumber) { try { _mDataOutputStream.writeBytes(sequenceNumber + " NO\n"); /*send.add(sequenceNumber + " NO");*/ } catch (IOException ex) {} }
}
