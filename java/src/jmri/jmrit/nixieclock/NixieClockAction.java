// NixieClockAction.java
package jmri.jmrit.nixieclock;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a NixieClockFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2004
 * @version	$Revision$
 */
public class NixieClockAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -5653182277242573672L;

    public NixieClockAction() {
        this("Nixie Clock");
    }

    public NixieClockAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {

        NixieClockFrame f = new NixieClockFrame();
        f.setVisible(true);

    }

}

/* @(#)NixieClockAction.java */
