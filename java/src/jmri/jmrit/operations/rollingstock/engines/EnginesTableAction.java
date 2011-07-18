// EnginesTableAction.java

package jmri.jmrit.operations.rollingstock.engines;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a EnginesTableFrame object.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class EnginesTableAction extends AbstractAction {
    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle");

    public EnginesTableAction(String s) {
	super(s);
    }

    public EnginesTableAction() {
        this(rb.getString("TitleEnginesTable"));
    }

    public void actionPerformed(ActionEvent e) {
        // create a engine table frame
        new EnginesTableFrame();
    }
}

/* @(#)EnginesTableAction.java */
