// XmlFileLocationAction.java

package jmri.jmrit;

import javax.swing.AbstractAction;
import javax.swing.JTextArea;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.*;

/**
 * Swing action to display the JMRI directory locations.
 *<P>
 * Although this has "XML" in it's name, it's actually much more
 * general.  It displays:
 *<ul>
 *<li>The preferences directory
 *<li>The program directory
 *<li>and any log files seen in the program directory
 *</ul>
 *
 * @author	    Bob Jacobsen    Copyright (C) 2004, 2007
 * @version         $Revision: 1.5 $
 */
public class XmlFileLocationAction extends AbstractAction {
    
    public XmlFileLocationAction() { super();}
    
    public void actionPerformed(ActionEvent ev) {
        
        JFrame frame = new jmri.util.JmriJFrame();  // to ensure fits
        
        JTextArea pane = new javax.swing.JTextArea();
        pane.setEditable(false);
        
        JScrollPane  scroll = new JScrollPane(pane);
        frame.getContentPane().add(scroll);
        
        String prefs = jmri.jmrit.XmlFile.prefsDir();
        pane.append("Preferences directory: "+prefs+"\n");
        
        String prog = System.getProperty("user.dir");
        pane.append("Program directory: "+prog+"\n");

        addLogFiles(pane);
                
        frame.pack();
        frame.setVisible(true);
    }
    
    void addLogFile(JTextArea pane, String filename) {
        File file = new File(filename);
        if (file.exists()) {
            pane.append("Log file: "+file.getAbsolutePath()+"\n");
        }
    }

    void addLogFiles(JTextArea pane) {
        File dir = new File(System.getProperty("user.dir"));
        String[] files = dir.list();
        for (int i=0; i<files.length; i++) {
            if (files[i].indexOf(".log")!=-1) {
                addLogFile(pane, files[i]);
            }
        }
    }
    
}

/* @(#)XmlFileLocationAction.java */
