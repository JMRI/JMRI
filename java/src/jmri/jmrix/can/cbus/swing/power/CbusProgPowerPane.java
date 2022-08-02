package jmri.jmrix.can.cbus.swing.power;

import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmrit.powerpanel.PowerPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for CBUS programming track power control
 * 
 * @author Andrew crosland (C) 2022
 */
public class CbusProgPowerPane extends PowerPane {
    
    CbusProgPowerPane() {
        super();
    }
    
    @Override
    protected int getPower() {
        return listening.getProgTrackPower();
    }
    
    /**
     * Helper function that may be overridden for other power interfaces
     * @param mode the power mode to set
     */
    @Override
    protected void setPower(int mode) throws JmriException {
        selectMenu.getManager().setProgTrackPower(mode);
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusProgPowerPane.class);
}
