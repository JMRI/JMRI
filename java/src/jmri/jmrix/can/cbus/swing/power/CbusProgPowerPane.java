package jmri.jmrix.can.cbus.swing.power;


import javax.swing.JLabel;

import jmri.JmriException;
import jmri.PowerManager;
import static jmri.PowerManager.PROGPOWER;
import jmri.jmrit.powerpanel.PowerPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for CBUS programming track power control
 * 
 * @author Andrew crosland (C) 2022
 */
public class CbusProgPowerPane extends PowerPane {
    
    public CbusProgPowerPane() {
        super();
    }
    
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.can.cbus.swing.power.ProgPowerPanelFrame";
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("MenuItemProgTrackPower");
    }

    @Override
    protected JLabel getPowerLabel() {
        return new JLabel(Bundle.getMessage("LabelProgTrackPower"));
    }
    
    @Override
    protected int getPower() {
        try {
            return listening.getProgTrackPower();
        } catch (JmriException e) {
            log.error("Exception trying to get power state", e);
            return PowerManager.UNKNOWN;
        }
    }
    
    /**
     * Helper function that may be overridden for other power interfaces
     * @param mode the power mode to set
     */
    @Override
    protected void setPower(int mode) throws JmriException {
        selectMenu.getManager().setProgTrackPower(mode);
    }
    
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent ev) {
        log.debug("PropertyChange received ");
        if (PowerManager.PROGPOWERENABLE.equals(ev.getPropertyName())) {
            onButton.setEnabled((boolean)ev.getNewValue());
            offButton.setEnabled((boolean)ev.getNewValue());
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusProgPowerPane.class);
}
