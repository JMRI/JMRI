// LV102Action.java

package jmri.jmrix.lenz.lv102;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import java.util.ResourceBundle;

/**
 * Swing action to create and register an LV102Frame object.
 * <P>
 * The {@link LV102Frame} is a configuration tool for the LV102 
 * booster, and the booster portion of an LZV100 command station.
 *
 * @author			Paul Bender    Copyright (C) 2004
 * @version			$Revision: 1.1 $
 */
public class LV102Action extends AbstractAction {

    public LV102Action(String s) { super(s);}
    public LV102Action() {

        this("LV102 Configuration Manager");
    }

    public void actionPerformed(ActionEvent e) {
        // create an LV102Frame
	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.lv102.LV102Bundle");
        LV102Frame f = new LV102Frame(rb.getString("LV102Config"));
        f.show();
    }
}

/* @(#)LV102Action.java */
