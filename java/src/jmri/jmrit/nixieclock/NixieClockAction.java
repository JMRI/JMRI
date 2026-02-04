package jmri.jmrit.nixieclock;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Swing action to create and register a NixieClockFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2004
 */
public class NixieClockAction extends JmriAbstractAction {

    public NixieClockAction() {
        this("Nixie Clock");
    }

    public NixieClockAction(String s) {
        super(s);
    }

    public NixieClockAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public NixieClockAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        NixieClockFrame f = new NixieClockFrame();
        f.setVisible(true);
    }

    @Override
    public jmri.util.swing.JmriPanel makePanel() { return null; } // not used here
}
