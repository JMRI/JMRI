package jmri.jmrix.can.cbus.swing.modeswitcher;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.ConfigurationManager;

/**
 * Create a ModeSwitcherPane.
 */
public class SprogCbusModeSwitcherAction extends AbstractAction {
    
    private CanSystemConnectionMemo _memo = null;

    public SprogCbusModeSwitcherAction(CanSystemConnectionMemo memo) {
        this(Bundle.getMessage("MenuItemSPROGModeSwitcher"), memo);
    //    _memo = memo;
    }

    public SprogCbusModeSwitcherAction(String s, CanSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SprogCbusModeSwitcherFrame f;
        
        if (_memo.getProgModeSwitch().equals(ConfigurationManager.ProgModeSwitch.EITHER)) {
            f = new SprogCbusSimpleModeSwitcherFrame(_memo);
        } else if (_memo.getProgModeSwitch().equals(ConfigurationManager.ProgModeSwitch.SPROG3PLUS)) {
            f = new SprogCbusSprog3PlusModeSwitcherFrame(_memo);
        } else {
            return;
        }
        f.initComponents();
        f.setVisible(true);
    }
}
