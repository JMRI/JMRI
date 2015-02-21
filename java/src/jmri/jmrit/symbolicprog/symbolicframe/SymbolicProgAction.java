/**
 * SymbolicProgAction.java
 *
 * Description:	Swing action to create and register a SymbolicProg object
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision$
 */
package jmri.jmrit.symbolicprog.symbolicframe;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class SymbolicProgAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 4028779777781438509L;

    public SymbolicProgAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {

        // create a SimpleProgFrame
        SymbolicProgFrame f = new SymbolicProgFrame();
        f.setVisible(true);

    }
}


/* @(#)SymbolicProgAction.java */
