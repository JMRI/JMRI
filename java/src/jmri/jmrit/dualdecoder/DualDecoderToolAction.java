// DualDecoderToolAction.java

package jmri.jmrit.dualdecoder;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * DualDecoderTool
 *
 * @author    Bob Jacobsen    Copyright (C) 2001
 * @version   $Revision$
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

        this(Bundle.getMessage("MenuItemMultiDecoderControl"));
    }

    public void actionPerformed(ActionEvent e) {

        new DualDecoderSelectFrame().setVisible(true);

    }

}

/* @(#)DualDecoderToolAction.java */
