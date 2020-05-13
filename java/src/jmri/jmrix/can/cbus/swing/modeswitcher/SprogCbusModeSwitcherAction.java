package jmri.jmrix.can.cbus.swing.modeswitcher;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * Create a ModeSwitcherPane.
 */
public class SprogCbusModeSwitcherAction extends AbstractAction {
    
    private CanSystemConnectionMemo _memo = null;

    public SprogCbusModeSwitcherAction(CanSystemConnectionMemo memo) {
        this(Bundle.getMessage("MenuItemSPROGModeSwitcher"), memo);
        _memo = memo;
    }

    public SprogCbusModeSwitcherAction(String s, CanSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        SprogCbusModeSwitcherFrame f = new SprogCbusModeSwitcherFrame(_memo);
        f.initComponents();
        f.setVisible(true);
    }

}
