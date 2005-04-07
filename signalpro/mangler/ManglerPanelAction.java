// ManglerPanelAction.java

package signalpro.mangler;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 * ManglerFrame object.
 *
 * @author	    Bob Jacobsen    Copyright (C) 2005
 * @version         $Revision: 1.1.1.1 $
 */
public class ManglerPanelAction extends AbstractAction {
    static ResourceBundle res = ResourceBundle.getBundle("signalpro.mangler.Mangler");

    public ManglerPanelAction(String s) {
	super(s);

	// disable ourself if there is no power Manager
        if (jmri.InstanceManager.powerManagerInstance()==null) {
            setEnabled(false);
        }
    }

    public ManglerPanelAction() {
        this(res.getString("TitleMangler"));
    }

    public void actionPerformed(ActionEvent e) {
        // create a PowerPanelFrame
        ManglerFrame f = new ManglerFrame();
        f.show();
    }
}

/* @(#)ManglerPanelAction.java */
