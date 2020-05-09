package jmri.jmrix.can.cbus.swing.modeswitcher;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Create a ModeSwitcherPane.
 */
public class SprogCbusModeSwitcherAction extends AbstractAction {

    public SprogCbusModeSwitcherAction() {
        this(Bundle.getMessage("MenuItemSPROGModeSwitcher"));
    }

    public SprogCbusModeSwitcherAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        SprogCbusModeSwitcherPane f = new SprogCbusModeSwitcherPane();
        f.initComponents();
        f.setVisible(true);
    }

}
