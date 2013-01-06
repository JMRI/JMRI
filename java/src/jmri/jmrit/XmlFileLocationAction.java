// XmlFileLocationAction.java

package jmri.jmrit;

import javax.swing.AbstractAction;
import javax.swing.JTextArea;
import java.awt.*;
import java.awt.event.*;
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
 * @version         $Revision$
 */
public class XmlFileLocationAction extends AbstractAction {
    
    public XmlFileLocationAction() { super();}
    
    public void actionPerformed(ActionEvent ev) {
        
        final String user = jmri.jmrit.XmlFile.userFileLocationDefault();
        final String roster = jmri.jmrit.roster.Roster.getFileLocation();
 		final String config = jmri.jmrit.XmlFile.configDir();
        final String configName = System.getProperty("org.jmri.Apps.configFilename");
        final String scripts = jmri.jmrit.XmlFile.scriptsDir();
        final String prog = System.getProperty("user.dir");

		
		
		
		
		
        JFrame frame = new jmri.util.JmriJFrame();  // to ensure fits
                
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        
        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout());
        pane.add(buttons);
        
        JButton b = new JButton("Open User Files Location");
        buttons.add(b);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
				try {
					java.awt.Desktop.getDesktop().open(new java.io.File(user));
				} catch (java.io.IOException e) {
				}
			}
        });
		b = new JButton("Open Roster Location");
        buttons.add(b);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
				try {
					java.awt.Desktop.getDesktop().open(new java.io.File(roster));
				} catch (java.io.IOException e) {
				}
			}
        });
        b = new JButton("Open Default Location");
        buttons.add(b);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
				try {
					java.awt.Desktop.getDesktop().open(new java.io.File(config));
				} catch (java.io.IOException e) {
				}
			}
        });
        b = new JButton("Open Scripts Location");
        buttons.add(b);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
				try {
					java.awt.Desktop.getDesktop().open(new java.io.File(scripts));
				} catch (java.io.IOException e) {
				}
			}
        });
        b = new JButton("Open Program Location");
        buttons.add(b);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
				try {
					java.awt.Desktop.getDesktop().open(new java.io.File(prog));
				} catch (java.io.IOException e) {
				}
			}
        });
        
        JScrollPane  scroll = new JScrollPane(pane);
        frame.getContentPane().add(scroll);
        
        JTextArea textPane = new javax.swing.JTextArea();
        textPane.setEditable(false);
        pane.add(textPane);
        
        textPane.append("User Files Location: "+user+"\n");
        
        textPane.append("Roster Location: "+roster+"\n");
        
        textPane.append("Default Location: "+config+"\n");
        
        textPane.append("Scripts Location: "+scripts+"\n");
		
        textPane.append("Program Location: "+prog+"\n");
		
        addLogFiles(textPane);
                
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
