// BlockBossAction.java

package jmri.jmrit.blockboss;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

/**
 * Swing action to create and register a
 * "Simple Signal Logic" GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.7 $
 */

public class BlockBossAction extends AbstractAction {

    public BlockBossAction(String s) { super(s);}
    public BlockBossAction() { super();}

    public void actionPerformed(ActionEvent e) {

        // create the frame
        JFrame f = new BlockBossFrame();
        f.show();
    }
}

/* @(#)BlockBossAction.java */
