// LocoBufferStatsAction.java

package jmri.jmrix.loconet.locobuffer;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.loconet.LnTrafficController;
import java.util.ResourceBundle;

/**
 * Create and register a LocoBufferStatsFrame object.
 *
 * @author			Alex Shepherd    Copyright (C) 2003
 * @version			$Revision: 1.4 $
 */
public class LocoBufferStatsAction extends AbstractAction {

    public LocoBufferStatsAction(String s) {
        super(s);

	// disable ourself if there is no locobuffer connection present
        if ( (!jmri.jmrix.loconet.locobuffer.LocoBufferAdapter.hasInstance())
             && (!jmri.jmrix.loconet.locobufferii.LocoBufferIIAdapter.hasInstance())
             && (!jmri.jmrix.loconet.locobufferusb.LocoBufferUsbAdapter.hasInstance()) ) {
            setEnabled(false);
        }
    }

    public LocoBufferStatsAction() {
        this(ResourceBundle.getBundle("jmri.jmrix.loconet.LocoNetBundle").getString("MenuItemLocoBufferStats"));
    }

    public void actionPerformed(ActionEvent e) {
        LocoBufferStatsFrame f = new LocoBufferStatsFrame();
        f.show();
    }
}

/* @(#)LocoBufferStatsAction.java */
