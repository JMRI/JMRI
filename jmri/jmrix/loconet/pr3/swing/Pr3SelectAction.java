// Pr3SelectAction.java

package jmri.jmrix.loconet.pr3.swing;

import jmri.util.JmriJFrame;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *  frame containing a Pr3SelectPane object.
 *
 * @author	Bob Jacobsen    Copyright (C) 2006, 2008
 * @version	$Revision: 1.1 $
 */
public class Pr3SelectAction extends AbstractAction {

    static final java.util.ResourceBundle rm = java.util.ResourceBundle.getBundle("jmri.jmrix.loconet.LocoNetBundle");

    public Pr3SelectAction() {
        this(rm.getString("MenuItemPr3ModeSelect"));
    }

	public Pr3SelectAction(String s) { 
	    super(s);
	}
    
    public void actionPerformed(ActionEvent e) {
        JmriJFrame f = new JmriJFrame(rm.getString("MenuItemPr3ModeSelect"));
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: "+ex.toString());
        }
        f.add(new Pr3SelectPane());
        // add window help
        f.addHelpMenu("package.jmri.jmrix.loconet.pr3.swing.Pr3Select", true);
        f.pack();
        f.setLocation(100,30);
        f.setVisible(true);
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Pr3SelectAction.class.getName());
}


/* @(#)Pr3SelectAction.java */
