package jmri.jmrix.can.cbus.swing.power;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * Create a CBUS Programming Track Power Control Pane.
 *
 * @author Andrew Crosland Copyright (C) 2022
 */
public class CbusProgPowerAction extends AbstractAction {
    
    private CanSystemConnectionMemo _memo = null;

    public CbusProgPowerAction(CanSystemConnectionMemo memo) {
        this(Bundle.getMessage("MenuItemProgTrackPower"), memo);
    }

    public CbusProgPowerAction(String s, CanSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CbusProgPowerPanelFrame f = new CbusProgPowerPanelFrame();
        
        f.initComponents();
        f.setVisible(true);
    }
}
