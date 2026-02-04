package jmri.jmrit.pragotronclock;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Swing action to create and register a PragotronClockFrame object.
 *
 * @author Petr Sidlo Copyright (C) 2019
 *
 * Based on Nixie clock by Bob Jacobsen.
 */
public class PragotronClockAction extends JmriAbstractAction {

    public PragotronClockAction() {
        this("Pragotron Clock");
    }

    public PragotronClockAction(String s) {
        super(s);
    }

    public PragotronClockAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public PragotronClockAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        PragotronClockFrame f = new PragotronClockFrame();
        f.setVisible(true);
    }

    @Override
    public jmri.util.swing.JmriPanel makePanel() { return null; } // not used here
}
