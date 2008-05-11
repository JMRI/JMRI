// LocoStatsAction.java

package jmri.jmrix.loconet.locostats;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import java.util.ResourceBundle;

/**
 * Create and register a LocoStatsFrame object.
 * <p>
 * Moved from loconet.locobuffer.LocoBufferStatsAction
 * 
 * @author			Alex Shepherd    Copyright (C) 2003
 * @author			Bob Jacobsen    Copyright (C) 2008
 * @version			$Revision: 1.1 $
 * @since 2.1.5
 */
public class LocoStatsAction extends AbstractAction {

    public LocoStatsAction(String s) {
        super(s);

	// disable ourself if there is no suitable connection present
        if ( (!jmri.jmrix.loconet.locobuffer.LocoBufferAdapter.hasInstance())
             && (!jmri.jmrix.loconet.locobufferii.LocoBufferIIAdapter.hasInstance())
             && (!jmri.jmrix.loconet.locobufferusb.LocoBufferUsbAdapter.hasInstance()) 
             && (!jmri.jmrix.loconet.pr2.PR2Adapter.hasInstance())
             && (!jmri.jmrix.loconet.pr3.PR3Adapter.hasInstance()) 
             ) {
            setEnabled(false);
        }
    }

    public LocoStatsAction() {
        this(ResourceBundle.getBundle("jmri.jmrix.loconet.LocoNetBundle").getString("MenuItemLocoStats"));
    }

    public void actionPerformed(ActionEvent e) {
        LocoStatsFrame f = new LocoStatsFrame();
        f.setVisible(true);
    }
}

/* @(#)LocoStatsAction.java */
