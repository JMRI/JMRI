// XmlFileLocationAction.java

package jmri.jmrit;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import java.awt.*;
import javax.swing.*;

/**
 * Swing action to display the JMRI directory locations
 *
 * @author	    Bob Jacobsen    Copyright (C) 2004, 2007
 * @version         $Revision: 1.3 $
 */
public class XmlFileLocationAction extends AbstractAction {
    
    public XmlFileLocationAction() { super();}
    
    public void actionPerformed(ActionEvent ev) {
        
        JFrame frame = new jmri.util.JmriJFrame();  // to ensure fits
        
        javax.swing.JTextArea pane = new javax.swing.JTextArea();
        pane.setEditable(false);
        
        JScrollPane  scroll = new JScrollPane(pane);
        frame.getContentPane().add(scroll);
        
        String prefs = jmri.jmrit.XmlFile.prefsDir();
        pane.append("Preferences directory: "+prefs+"\n");
        
        String prog = System.getProperty("user.dir");
        pane.append("Program directory: "+prog+"\n");
        
        frame.pack();
        frame.setVisible(true);
    }
}

/* @(#)XmlFileLocationAction.java */
