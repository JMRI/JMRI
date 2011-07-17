// SimpleProgAction.java

 package jmri.jmrit.simpleprog;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			SimpleProgAction object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001, 2008
 * @version			$Revision: 1.7 $
 */public class SimpleProgAction 			extends AbstractAction {

     public SimpleProgAction(String s) {
         super(s);

     // disable ourself if programming is not possible
        if (jmri.InstanceManager.programmerManagerInstance()==null) { 
            setEnabled(false);
        }
     }
     
     public SimpleProgAction() {
        this(java.util.ResourceBundle.getBundle("jmri.jmrit.JmritToolsBundle")
            .getString("MenuItemSingleCVProgrammer"));
     }
     
     public void actionPerformed(ActionEvent e) {
         
         // create a SimpleProgFrame
         SimpleProgFrame f = new SimpleProgFrame();
         f.setVisible(true);
         
     }
 }

/* @(#)SimpleProgAction.java */
