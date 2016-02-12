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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final static Logger log = LoggerFactory.getLogger(SimpleLightCtrlAction.class.getName());
}


/* @(#)SimpleLightCtrlAction.java */
