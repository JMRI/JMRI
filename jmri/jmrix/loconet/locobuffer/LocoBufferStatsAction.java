// LocoBufferStatsAction.java

package jmri.jmrix.loconet.locobuffer;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.loconet.LnTrafficController;


/**
 * Create and register a LocoBufferStatsFrame object.
 *
 * @author			Alex Shepherd    Copyright (C) 2003
 * @version			$Revision: 1.1 $
 */
public class LocoBufferStatsAction extends AbstractAction {

    public LocoBufferStatsAction(String s) { super(s);}
    public LocoBufferStatsAction() {
        this("LocoBuffer Stats Monitor");
    }

    public void actionPerformed(ActionEvent e) {
        LocoBufferStatsFrame f = new LocoBufferStatsFrame();
        f.show();
    }
}

/* @(#)LocoBufferStatsAction.java */
