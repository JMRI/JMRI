// LV102Action.java
package jmri.jmrix.lenz.swing.lv102;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register an LV102Frame object.
 * <P>
 * The {@link LV102Frame} is a configuration tool for the LV102 booster, and the
 * booster portion of an LZV100 command station.
 *
 * @author	Paul Bender Copyright (C) 2004
 * @version	$Revision$
 */
public class LV102Action extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 8355340585591242351L;

    public LV102Action(String s) {
        super(s);
    }

    public LV102Action() {

        this("LV102 Configuration Manager");
    }

    public void actionPerformed(ActionEvent e) {
        // create an LV102Frame
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.swing.lv102.LV102Bundle");
        LV102Frame f = new LV102Frame(rb.getString("LV102Config"));
        f.setVisible(true);
    }
}

/* @(#)LV102Action.java */
