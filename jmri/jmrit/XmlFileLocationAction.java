// XmlFileLocationAction.java

package jmri.jmrit;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import java.awt.*;
import javax.swing.*;

/**
 * Swing action to display the JMRI directory locations
 *
 * @author	    Bob Jacobsen    Copyright (C) 2004
 * @version         $Revision: 1.1 $
 */
public class XmlFileLocationAction extends AbstractAction {

    public XmlFileLocationAction() { super();}

    public void actionPerformed(ActionEvent ev) {

		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

        String prefs = jmri.jmrit.XmlFile.prefsDir();
        frame.getContentPane().add(new JLabel("Preferences directory: "+prefs+"  "));
        
        String prog = System.getProperty("user.dir");
        frame.getContentPane().add(new JLabel("Program directory: "+prog+"  "));
        		
		frame.pack();
		frame.setVisible(true);
    }
}

/* @(#)XmlFileLocationAction.java */
