// LnSensorManager.java

package jmri.jmrix.loconet;

import org.apache.log4j.Logger;
import jmri.Sensor;
import jmri.JmriException;

/**
 * Manage the LocoNet-specific Sensor implementation.
 *
 * System names are "LSnnn", where nnn is the sensor number without padding.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision$
 */
public class LnSensorManager extends jmri.managers.AbstractSensorManager implements LocoNetListener {

    public LnSensorManager(LnTrafficController tc, String prefix) {
        this.prefix = prefix;
        this.tc = tc;
        // ctor has to register for LocoNet events
        tc.addLocoNetListener(~0, this);
        
        // start the update sequence.  Until JMRI 2.9.4, this waited
        // until files have been read, but was stated automatically 
        // in 2.9.5 for multi-system support.
        updateAll();
    }
    
    LnTrafficController tc;
    String prefix = "L";
    
    public String getSystemPrefix() { return prefix; }

    // to free resources when no longer used
    public void dispose() {
        tc.removeLocoNetListener(~0, this);
        super.dispose();
    }

    // LocoNet-specific methods

    public Sensor createNewSensor(String systemName, String userName) {
        return new LnSensor(systemName, userName, tc, prefix);
    }

    // listen for sensors, creating them as needed
    public void message(LocoNetMessage l) {
        // parse message type
        LnSensorAddress a;
        switch (l.getOpCode()) {
            case LnConstants.OPC_INPUT_REP:                /* page 9 of Loconet PE */
                int sw1 = l.getElement(1);
                int sw2 = l.getElement(2);
                a = new LnSensorAddress(sw1, sw2, prefix);
                if (log.isDebugEnabled()) log.debug("INPUT_REP received with address "+a);
                break;
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

    @SuppressWarnings("unused")
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
			LnSensorUpdateThread thread = new LnSensorUpdateThread(this, tc);
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

    public boolean allowMultipleAdditions(String systemName) { return true;  }

    public String createSystemName(String curAddress, String prefix) throws JmriException{
        if(curAddress.contains(":")){
            int board = 0;
            int channel = 0;
            //Address format passed is in the form of board:channel or T:turnout address
            int seperator = curAddress.indexOf(":");
            boolean turnout = false;
            if (curAddress.substring(0,seperator).toUpperCase().equals("T")){
                turnout = true;
            } else {
                try {
                    board = Integer.valueOf(curAddress.substring(0,seperator)).intValue();
                } catch (NumberFormatException ex) { 
                    log.error("Unable to convert " + curAddress + " into the cab and channel format of nn:xx");
                    throw new JmriException("Hardware Address passed should be a number");
                }
            }
            try {
                channel = Integer.valueOf(curAddress.substring(seperator+1)).intValue();
            } catch (NumberFormatException ex) { 
                log.error("Unable to convert " + curAddress + " into the cab and channel format of nn:xx");
                throw new JmriException("Hardware Address passed should be a number");
            }
            if (turnout){
                iName = 2 * (channel-1)+1;
            } else {
                iName = 16*board + channel - 16;
            }
        } else {
            //Entered in using the old format
            try {
                iName = Integer.parseInt(curAddress);
            } catch (NumberFormatException ex) { 
                log.error("Unable to convert " + curAddress + " Hardware Address to a number");
                throw new JmriException("Hardware Address passed should be a number");
            }
        }
        return prefix+typeLetter()+iName;
    
    }
    int iName;

    
    public String getNextValidAddress(String curAddress, String prefix){

        String tmpSName = "";

        try {
            tmpSName = createSystemName(curAddress, prefix);
        } catch (JmriException ex) {
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showInfoMessage("Error","Unable to convert " + curAddress + " to a valid Hardware Address",""+ex, "",true, false, org.apache.log4j.Level.ERROR);
            return null;
        }
        
        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        Sensor s = getBySystemName(tmpSName);
        if(s!=null){
            for(int x = 1; x<10; x++){
                iName=iName+1;
                s = getBySystemName(prefix+typeLetter()+iName);
                if(s==null){
                    return Integer.toString(iName);
                }
            }
            return null;
        } else {
            return Integer.toString(iName);
        }
    }
    
    static Logger log = Logger.getLogger(LnSensorManager.class.getName());

}

/**
 * Class providing a thread to update sensor states
 */
class LnSensorUpdateThread extends Thread
{
	/**
	 * Constructs the thread
	 */
	public LnSensorUpdateThread (LnSensorManager sm, LnTrafficController tc) {
		this.sm = sm;
		this.tc = tc;
	}
	
	/** 
	 * Runs the thread - sends 8 commands to query status of all stationary sensors
	 *     per LocoNet PE Specs, page 12-13
	 * Thread waits 500 msec between commands.
	 */
	public void run () {
	    sm.setUpdateBusy();
		byte sw1[] = {0x78,0x79,0x7a,0x7b,0x78,0x79,0x7a,0x7b};
		byte sw2[] = {0x27,0x27,0x27,0x27,0x07,0x07,0x07,0x07};
		// create and initialize loconet message
        LocoNetMessage m = new LocoNetMessage(4);
        m.setOpCode(LnConstants.OPC_SW_REQ);
		for (int k = 0; k < 8; k++) {
			try {
				Thread.sleep(500);
			}
			catch (InterruptedException e) {
			    Thread.currentThread().interrupt(); // retain if needed later
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
