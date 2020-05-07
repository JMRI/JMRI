package jmri.jmrix.can.cbus.swing.modeswitcher;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Create a ModeSwitcherPane.
 */
public class CbusModeSwitcherAction extends AbstractAction {

    public CbusModeSwitcherAction() {
        this(Bundle.getMessage("MenuItemSPROGModeSwitcher"));
    }

    public CbusModeSwitcherAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        CbusModeSwitcherPane f = new CbusModeSwitcherPane();
        f.initComponents();
        f.setVisible(true);
    }

}
