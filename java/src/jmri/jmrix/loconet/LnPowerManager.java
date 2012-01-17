// LnPowerManager.java

package jmri.jmrix.loconet;

import jmri.PowerManager;
import jmri.JmriException;

/**
 * PowerManager implementation for controlling layout power
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version         $Revision$
 */
public class LnPowerManager 
        extends jmri.managers.AbstractPowerManager
        implements PowerManager, LocoNetListener {

	public LnPowerManager(LocoNetSystemConnectionMemo memo) {
	    super(memo);
	    // standard LocoNet - connect
        if(memo.getLnTrafficController()==null){
            log.error("Power Manager Created, yet there is no Traffic Controller");
            return;
        }
        this.tc = memo.getLnTrafficController();
		tc.addLocoNetListener(~0, this);
	}

	protected int power = UNKNOWN;

	public void setPower(int v) throws JmriException {
		power = UNKNOWN;
		
        checkTC();
        if (v==ON) {
            // send GPON
            LocoNetMessage l = new LocoNetMessage(2);
            l.setOpCode(LnConstants.OPC_GPON);
            tc.sendLocoNetMessage(l);
        } else if (v==OFF) {
            // send GPOFF
            LocoNetMessage l = new LocoNetMessage(2);
            l.setOpCode(LnConstants.OPC_GPOFF);
            tc.sendLocoNetMessage(l);
        }

		firePropertyChange("Power", null, null);
	}

	public int getPower() { return power;}

	// to free resources when no longer used
	public void dispose() {
		if (tc!=null) tc.removeLocoNetListener(~0, this);
		tc = null;
	}

	LnTrafficController tc = null;
	
	private void checkTC() throws JmriException {
		if (tc == null) throw new JmriException("Use power manager after dispose");
	}

	// to listen for status changes from LocoNet
	public void message(LocoNetMessage m) {
		if (m.getOpCode() == LnConstants.OPC_GPON) {
			power = ON;
			firePropertyChange("Power", null, null);
		}
		else if (m.getOpCode() == LnConstants.OPC_GPOFF) {
			power = OFF;
			firePropertyChange("Power", null, null);
		}
	}
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LnPowerManager.class.getName());

}

/* @(#)LnPowerManager.java */
