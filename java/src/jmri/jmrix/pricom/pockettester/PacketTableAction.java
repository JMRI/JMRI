package jmri.jmrix.pricom.pockettester;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a PacketTable frame.
 *
 * @author	Bob Jacobsen Copyright (C) 2005
 */
public abstract class PacketTableAction extends AbstractAction {

    public PacketTableAction(String s) {
        super(s);
    }

    public PacketTableAction() {
        super();
        putValue(javax.swing.Action.NAME, Bundle.getMessage("ActionPacketTable"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // create the frame & show
        PacketTableFrame f = new PacketTableFrame();
        f.initComponents();
        connect(f);
        f.setVisible(true);

    }

    abstract void connect(DataListener l);

}
