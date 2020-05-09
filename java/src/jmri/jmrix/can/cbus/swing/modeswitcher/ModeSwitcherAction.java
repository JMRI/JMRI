package jmri.jmrix.can.cbus.swing.modeswitcher;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Create a ModeSwitcherPane.
 */
public class ModeSwitcherAction extends AbstractAction {

    public ModeSwitcherAction() {
        this(Bundle.getMessage("MenuItemSPROGModeSwitcher"));
    }

    public ModeSwitcherAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        ModeSwitcherPane f = new ModeSwitcherPane();
        f.initComponents();
        f.setVisible(true);
    }

}
