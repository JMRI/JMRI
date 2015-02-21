// PacketTableAction.java
package jmri.jmrix.pricom.pockettester;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a PacketTable frame.
 *
 * @author	Bob Jacobsen Copyright (C) 2005
 * @version $Revision$
 */
public abstract class PacketTableAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -755408790466810109L;

    public PacketTableAction(String s) {
        super(s);
    }

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
        f.setVisible(true);

    }

    abstract void connect(DataListener l);

}


/* @(#)PacketTableAction.java */
