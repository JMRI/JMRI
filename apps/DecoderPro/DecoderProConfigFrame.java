// DecoderProConfigFrame.java

package jmri.apps;

import jmri.apps.DecoderProConfigFile;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/** 
 * DecoderProConfigFrame provides startup configuration, a GUI for setting 
 * config/preferences, and read/write support.  Its specific to DecoderPro
 * but should eventually be generalized.  Note that routine GUI config,
 * menu building, etc is done in other code.
 *<P>For now, we're implicitly assuming that configuration of these
 * things is _only_ done here, so that we don't have to track anything
 * else.  When asked to write the config, we just write the values
 * stored in local variables.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: DecoderProConfigFrame.java,v 1.1.1.1 2001-12-02 05:51:21 jacobsen Exp $
 */
public class DecoderProConfigFrame extends JFrame {
		
	public DecoderProConfigFrame(String name) {
		super(name);
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		// create the GUI in steps
		getContentPane().add(createConnectionPane());
		getContentPane().add(createGUIPane());
		getContentPane().add(createProgrammerPane());
		
		JButton save = new JButton("Save");
		getContentPane().add(save);
		save.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				savePressed();
			}
		});
		
		// show is deferred to some action somewhere else
		pack();
		}
		
	
	/**
	 * Command reading the configuration, and setting it into the application.
	 * Returns true if
	 * a configuration file was found and loaded OK.
	 */
	public boolean readConfig() {
		return true;
	}
	
	/**
	 * Command writing the current configuration, without setting it.
	 * Returns true if
	 * a configuration file was found and loaded OK.
	 */
	public boolean writeConfig() {
		return true;
	}
	
	/**
	 * Handle the Save button:  Backup the file, write a new one, close the frame.
	 */
	public void savePressed() {
	}
	
	JComboBox protocolBox;
	JComboBox portBox;
	/*
	 * Create a panel showing the valid connection methods and port names
	 */
	JPanel createConnectionPane() {
		JPanel j = new JPanel();
		
		JLabel l;
		
		j.setLayout(new GridLayout(2,2));
		protocolBox = new JComboBox(new String[] {"(None selected)", "LocoNet MS100", 
													"LocoNet LocoBuffer", "NCE"});
		protocolBox.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				protocolSelected();
			}
		});
		protocolBox.setToolTipText("Select a connection method");
		l = new JLabel("Layout connection: ");
		j.add(l);
		j.add(protocolBox);
		
		portBox = new JComboBox(new String[] {"(select a connection method first)"});
		portBox.setToolTipText("This is disabled until you select a connection method");
		portBox.setEnabled(false);
		
		l = new JLabel("Serial port: ");
		j.add(l);
		j.add(portBox);
		
		return j;
	}
	
	/*
	 * Connection method has been selected; show available ports
	 */
	void protocolSelected() {
		portBox.setEnabled(true);
		portBox.setToolTipText("Select a communications port");
		
		// create the eventual serial driver object, and ask it for available comm ports
		String name = (String) protocolBox.getSelectedItem();
		portBox.removeAllItems();  // start over
		log.debug("Connection selected: "+name);
		if (name.equals("LocoNet LocoBuffer")) {
			//
			jmri.jmrix.loconet.locobuffer.LocoBufferAdapter a 
					= new jmri.jmrix.loconet.locobuffer.LocoBufferAdapter();
			Vector v = a.getPortNames();
			log.debug("Found "+v.size()+" ports");
			for (int i=0; i<v.size(); i++) {
				portBox.addItem(v.elementAt(i));
			}
			
		} else if (name.equals("LocoNet MS100")) {
			//
			jmri.jmrix.loconet.locobuffer.LocoBufferAdapter a 
					= new jmri.jmrix.loconet.locobuffer.LocoBufferAdapter();
			Vector v = a.getPortNames();
			log.debug("Found "+v.size()+" ports");
			for (int i=0; i<v.size(); i++) {
				portBox.addItem(v.elementAt(i));
			}
			
		} else if (name.equals("NCE")) {
			//
			jmri.jmrix.loconet.locobuffer.LocoBufferAdapter a 
					= new jmri.jmrix.loconet.locobuffer.LocoBufferAdapter();
			Vector v = a.getPortNames();
			log.debug("Found "+v.size()+" ports");
			for (int i=0; i<v.size(); i++) {
				portBox.addItem(v.elementAt(i));
			}
			
		} else {
			// selected nothing, so put it back as it was
				portBox.addItem("(select a connection method first)");
				portBox.setToolTipText("This is disabled until you select a connection method");
				portBox.setEnabled(false);
		}
	}
	jmri.jmrix.SerialDriverAdapter port = null;
	
	boolean configureConnection() {
		return true;
	}
	
	/*
	 * Create a panel showing the valid Swing Look&Feels and allowing selection
	 */
	JPanel createGUIPane() {
		JPanel c = new JPanel();
		c.setLayout(new FlowLayout());
		
		// find L&F definitions
        UIManager.LookAndFeelInfo[] plafs = UIManager.getInstalledLookAndFeels();
        installedLAFs = new java.util.Hashtable(plafs.length);
        for (int i = 0; i < plafs.length; i++){
            installedLAFs.put(plafs[i].getName(), plafs[i].getClassName());
        }
		// make the radio buttons
        LAFGroup = new ButtonGroup();
        Enumeration LAFNames = installedLAFs.keys();
        while (LAFNames.hasMoreElements()) {
            String name = (String)LAFNames.nextElement();
            JRadioButton jmi = new JRadioButton(name);
            c.add(jmi);
            LAFGroup.add(jmi);
            jmi.setActionCommand(name);
            jmi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					log.info("LAF set to "+e.getActionCommand());
					selectedLAF = e.getActionCommand();
				}
            });
            if (installedLAFs.get(name).equals(UIManager.getLookAndFeel().getClass().getName())) {
                jmi.setSelected(true);
                selectedLAF = name;
            }
        } 
 		return c;
	}
	java.util.Hashtable installedLAFs;
	ButtonGroup LAFGroup;
	String selectedLAF;
	
	boolean configureGUI() {
		return true;
	}

	JPanel createProgrammerPane() {
		JPanel j = new JPanel();
		j.setLayout(new BoxLayout(j, BoxLayout.Y_AXIS));
		return j;
	}
	
	boolean configureProgrammer() {
		return true;
	}

	
	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderProConfigFrame.class.getName());
		
}
