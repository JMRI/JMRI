// PacketTableAction.java

package jmri.jmrix.pricom.pockettester;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * 
 *
 * @author	Bob Jacobsen    Copyright (C) 2005
 * @version     $Revision: 1.2 $
 */

public abstract class PacketTableAction extends AbstractAction {

    public PacketTableAction(String s) { super(s);}
    public PacketTableAction() { 
        super();
        java.util.ResourceBundle rb 
            = java.util.ResourceBundle.getBundle("jmri.jmrix.pricom.pockettester.TesterBundle");
        putValue(javax.swing.Action.NAME, rb.getString("ActionPacketTable"));
    }

    public void actionPerformed(ActionEvent e) {

        // create the frame & show
        PacketTableFrame f = new PacketTableFrame();
        f.initComponents();
        connect(f);
        f.show();

    }
    
    abstract void connect(DataListener l);
    
}


/* @(#)PacketTableAction.java */
