package jmri.jmrit.nixieclock;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a NixieClockFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2004
 */
public class NixieClockAction extends AbstractAction {

    public NixieClockAction() {
        this("Nixie Clock");
    }

    public NixieClockAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        NixieClockFrame f = new NixieClockFrame();
        f.setVisible(true);
    }

}
