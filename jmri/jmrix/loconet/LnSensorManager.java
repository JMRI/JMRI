// LnSensorManager.java

package jmri.jmrix.loconet;

import jmri.Sensor;

/**
 * Manage the LocoNet-specific Sensor implementation.
 *
 * System names are "LSnnn", where nnn is the sensor number without padding.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.12 $
 */
public class LnSensorManager extends jmri.AbstractSensorManager implements LocoNetListener {

    public char systemLetter() { return 'L'; }

    static public LnSensorManager instance() {
        if (mInstance == null) new LnSensorManager();
        return mInstance;
    }
    static private LnSensorManager mInstance = null;

    // to free resources when no longer used
    public void dispose() {
        LnTrafficController.instance().removeLocoNetListener(~0, this);
    }

    // LocoNet-specific methods

    public Sensor createNewSensor(String systemName, String userName) {
        return new LnSensor(systemName, userName);
    }

    // ctor has to register for LocoNet events
    public LnSensorManager() {
        LnTrafficController.instance().addLocoNetListener(~0, this);
        mInstance = this;
    }

    // listen for sensors, creating them as needed
    public void message(LocoNetMessage l) {
        // parse message type
        LnSensorAddress a;
        switch (l.getOpCode()) {
        case LnConstants.OPC_INPUT_REP: {               /* page 9 of Loconet PE */
            int sw1 = l.getElement(1);
            int sw2 = l.getElement(2);
            a = new LnSensorAddress(sw1, sw2);
            if (log.isDebugEnabled()) log.debug("INPUT_REP received with address "+a);
            break;
        }
        default:  // here we didn't find an interesting command
            return;
        }
        // reach here for loconet sensor input command; make sure we know about this one
        String s = a.getNumericAddress();
        if (null == getBySystemName(s)) {
            // need to store a new one
            if (log.isDebugEnabled()) log.debug("Create new LnSensor as "+s);
            LnSensor ns = (LnSensor)newSensor(s, null);
            ns.message(l);  // have it update state
        }
    }

    private int address(int a1, int a2) {
        // the "+ 1" in the following converts to throttle-visible numbering
        return (((a2 & 0x0f) * 128) + (a1 & 0x7f) + 1);
    }
	
    /**
     * Requests status updates from all layout sensors.
	 */
	public void updateAll() {
		if (!busy) {
			setUpdateBusy();
			LnSensorUpdateThread thread = new LnSensorUpdateThread(this);
			thread.start();
		}
	}

    /**
     * Method to set Route busy when commands are being issued to 
     *   Route turnouts
	 */
    public void setUpdateBusy() {
		busy = true;
	}

    /**
     * Method to set Route not busy when all commands have been
     *   issued to Route turnouts
	 */
    public void setUpdateNotBusy() {
		busy = false;
	}
	
	private boolean busy = false;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnSensorManager.class.getName());

}

/**
 * Class providing a thread to update sensor states
 */
class LnSensorUpdateThread extends Thread
{
	/**
	 * Constructs the thread
	 */
	public LnSensorUpdateThread (LnSensorManager sensorManager) {
		sm = sensorManager;
		tc = LnTrafficController.instance();
	}
	
	/** 
	 * Runs the thread - sends 8 commands to query status of all stationary sensors
	 *     per LocoNet PE Specs, page 12-13
	 * Thread waits 500 msec between commands after a 2 sec initial wait.
	 */
	public void run () {
		byte sw1[] = {0x78,0x79,0x7a,0x7b,0x78,0x79,0x7a,0x7b};
		byte sw2[] = {0x27,0x27,0x27,0x27,0x07,0x07,0x07,0x07};
		// create and initialize loconet message
        LocoNetMessage m = new LocoNetMessage(4);
        m.setOpCode(LnConstants.OPC_SW_REQ);
		try {
			Thread.sleep(1500);
		}
		catch (InterruptedException e) {
		}
		for (int k = 0; k < 8; k++) {
			try {
				Thread.sleep(500);
			}
			catch (InterruptedException e) {
				break;
			}
			m.setElement(1,sw1[k]);
			m.setElement(2,sw2[k]);
			tc.sendLocoNetMessage(m);
		}
		sm.setUpdateNotBusy();
	}
	
	private LnSensorManager sm = null;
	private LnTrafficController tc = null;

}

/* @(#)LnSensorManager.java */
