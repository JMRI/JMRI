// Dcc4PcSensor.java
package jmri.jmrix.dcc4pc;

import jmri.implementation.AbstractSensor;

/**
 * Implement a Sensor via Dcc4Pc communications.
 * <P>
 * This object doesn't listen to the Dcc4Pc communications. This is because it
 * should be the only object that is sending messages for this sensor; more than
 * one Sensor object pointing to a single device is not allowed.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @author Kevin Dickerson (C) 2012
 * @version	$Revision: 17977 $
 */
public class Dcc4PcSensor extends AbstractSensor {

    /**
     *
     */
    private static final long serialVersionUID = -4792333997713777957L;

    public Dcc4PcSensor(String systemName, String userName) {
        super(systemName, userName);
        init(systemName);
    }

    public Dcc4PcSensor(String systemName) {
        super(systemName);
        init(systemName);
    }

    //private int boardAddress;
    private int inputLine;

    private void init(String id) {
    }

    public void requestUpdateFromLayout() {
    }

    static String[] modeNames = null;
    static int[] modeValues = null;

    public void setOwnState(int state) {
        int stateConvert = UNKNOWN;
        realState = state;
        switch (state) {
            case INACTIVE:
                stateConvert = INACTIVE;
                break; //"UnOccupied"
            case ACTIVE:
                stateConvert = ACTIVE;
                break; //"Occupied No RailComm Data"
            case ORIENTA:
                stateConvert = ACTIVE;
                break; //"Occupied RailComm Orientation A"
            case ORIENTB:
                stateConvert = ACTIVE;
                break; //"Occupied RailComm Orientation B"
            default:
                stateConvert = UNKNOWN;
                break;
        }
        super.setOwnState(stateConvert);
    }

    final public static int ORIENTA = 0x10;
    final public static int ORIENTB = 0x20;
    int realState = UNKNOWN;

    public int getRailCommState() {
        return realState;
    }

    public void notifyReply(Dcc4PcReply r) {
    }

    public void notifyMessage(Dcc4PcMessage m) {
    }

    boolean enabled = false;

    public void setEnabled(boolean enable) {
        enabled = enable;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setInput(int i) {
        inputLine = i;
    }

    public int getInput() {
        return inputLine;
    }
}

/* @(#)Dcc4PcSensor.java */
