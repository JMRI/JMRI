package jmri.jmrix.can.cbus.swing.power;

/**
 * Create a CBUS Programming Track Power Control Pane.
 *
 * @author Andrew Crosland Copyright (C) 2022
 */
public class CbusProgPowerAction extends jmri.util.swing.JmriNamedPaneAction {
    
    public CbusProgPowerAction(String s, jmri.util.swing.WindowInterface wi) {
        super(s, wi, "jmri.jmrix.can.cbus.swing.power.CbusProgPowerPane");
        checkManager();
    }

    public CbusProgPowerAction(String s, javax.swing.Icon i, jmri.util.swing.WindowInterface wi) {
        super(s, i, wi, "jmri.jmrix.can.cbus.swing.power.CbusProgPowerPane");
        checkManager();
    }

    public CbusProgPowerAction(String s) {
        super(s, "jmri.jmrix.can.cbus.swing.power.CbusProgPowerPane");
        checkManager();
    }

    public CbusProgPowerAction() {
        this(Bundle.getMessage("LabelProgTrackPower"));
    }

    protected void checkManager() {
        // disable ourself if there is no power Manager
        if (jmri.InstanceManager.getNullableDefault(jmri.PowerManager.class) == null) {
            setEnabled(false);
        }
    }

}
