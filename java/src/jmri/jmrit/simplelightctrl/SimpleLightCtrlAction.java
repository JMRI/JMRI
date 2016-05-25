/**
 * SimpleLightCtrlAction.java
 *
 * Description:	Swing action to create and register a SimpleTurnoutCtrlFrame
 * object
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version
 */
package jmri.jmrit.simplelightctrl;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class SimpleLightCtrlAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 4405267193635229462L;

    public SimpleLightCtrlAction(String s) {
        super(s);

        // disable ourself if there is no primary light manager available
        if (jmri.InstanceManager.lightManagerInstance() == null) {
            setEnabled(false);
        }

    }

    public SimpleLightCtrlAction() {
        this("Lights");
    }

    public void actionPerformed(ActionEvent e) {

        SimpleLightCtrlFrame f = new SimpleLightCtrlFrame();
        f.setVisible(true);

    }
}


/* @(#)SimpleLightCtrlAction.java */
