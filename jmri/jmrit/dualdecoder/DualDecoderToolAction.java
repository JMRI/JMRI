// DualDecoderToolAction.java

package jmri.jmrit.dualdecoder;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * DualDecoderTool
 *
 * @author    Bob Jacobsen    Copyright (C) 2001
 * @version   $Revision: 1.3 $
 */

public class DualDecoderToolAction extends AbstractAction {

    public DualDecoderToolAction(String s) {
        super(s);

        // disable ourself if programming is not possible
        if (jmri.InstanceManager.programmerManagerInstance()==null) {
            setEnabled(false);
        }

    }

    public DualDecoderToolAction() {
        this("Multi-decoder control");
    }

    public void actionPerformed(ActionEvent e) {

        new DualDecoderSelectFrame().show();

    }

}

/* @(#)DualDecoderToolAction.java */
