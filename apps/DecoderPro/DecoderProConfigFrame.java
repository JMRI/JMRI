// DecoderProConfigFrame.java

package jmri.apps;

import jmri.apps.DecoderProConfigFile;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.jdom.Element;
import org.jdom.Attribute;

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
 * @version			$Id: DecoderProConfigFrame.java,v 1.5 2001-12-18 07:21:33 jacobsen Exp $
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
	public boolean configure(DecoderProConfigFile file) throws jmri.JmriException {
		boolean connected = configureConnection(file.getConnectionElement());
		boolean gui = configureGUI(file.getGuiElement());
		boolean programmer = configureProgrammer(file.getProgrammerElement());
		return connected&&gui&&programmer;
	}
	
	/**
	 * Command writing the current configuration, without setting it.
	 * Returns true if
	 * a configuration file was found and loaded OK.
	 */
	//public boolean writeConfig() {
	//	return true;
	//}
	
	/**
	 * Handle the Save button:  Backup the file, write a new one, close the frame.
	 */
	public void savePressed() {
		DecoderProConfigFile f = new DecoderProConfigFile();
		f.makeBackupFile(DecoderProConfigFile.defaultConfigFilename());
		f.writeFile(DecoderProConfigFile.defaultConfigFilename(), this);
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
		portBox.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				portSelected();
			}
		});
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
		protocolName = (String) protocolBox.getSelectedItem();
		portBox.removeAllItems();  // start over
		log.debug("Connection selected: "+protocolName);
		if (protocolName.equals("LocoNet LocoBuffer")) {
			//
			jmri.jmrix.loconet.locobuffer.LocoBufferAdapter a 
					= new jmri.jmrix.loconet.locobuffer.LocoBufferAdapter();
			Vector v = a.getPortNames();
			log.debug("Found "+v.size()+" LocoBuffer ports");
			for (int i=0; i<v.size(); i++) {
				if (i==0) portName = (String) v.elementAt(i);
				portBox.addItem(v.elementAt(i));
			}
			
		} else if (protocolName.equals("LocoNet MS100")) {
			//
			jmri.jmrix.loconet.ms100.MS100Adapter a 
					= new jmri.jmrix.loconet.ms100.MS100Adapter();
			Vector v = a.getPortNames();
			log.debug("Found "+v.size()+" MS100 ports");
			for (int i=0; i<v.size(); i++) {
				if (i==0) portName = (String) v.elementAt(i);
				portBox.addItem(v.elementAt(i));
			}
			
		} else if (protocolName.equals("NCE")) {
			//
			jmri.jmrix.nce.serialdriver.SerialDriverAdapter a 
					= new jmri.jmrix.nce.serialdriver.SerialDriverAdapter();
			Vector v = a.getPortNames();
			log.debug("Found "+v.size()+" NCE ports");
			for (int i=0; i<v.size(); i++) {
				if (i==0) portName = (String) v.elementAt(i);
				portBox.addItem(v.elementAt(i));
			}
			
		} else {
			// selected nothing, so put it back as it was
				portBox.addItem("(select a connection method first)");
				portBox.setToolTipText("This is disabled until you select a connection method");
				portBox.setEnabled(false);
		}
	}
	/*
	 * Port name has been selected; store
	 */
	void portSelected() {
		portName = (String) portBox.getSelectedItem();
	}

	jmri.jmrix.SerialPortAdapter port = null;
	String protocolName = "(None selected)";
	String portName = "(None selected)";
	
	Element getConnection() {
		Element e = new Element("connection");
		e.addAttribute("class", protocolName);
		e.addAttribute("port", portName);
		return e;
	}
	
	boolean configureConnection(Element e) throws jmri.JmriException {
		protocolName = e.getAttribute("class").getValue();
		protocolBox.setSelectedItem(protocolName);
		// note that the line above will _change_ the value of portName, as it 
		// selects a default

		portName = e.getAttribute("port").getValue();
		portBox.setSelectedItem(e.getAttribute("port").getValue());
		portName = e.getAttribute("port").getValue();
		
		// check that the specified port exists		
		if (!e.getAttribute("port").getValue().equals(portBox.getSelectedItem())) {
			// can't connect to a non-existant port!
			log.error("Configured port \""+portName+"\" doesn't exist, no connection to layout made");
			return false;
		}
		
		// handle the specific case (a good use for reflection!)
		log.info("Configuring connection with "+protocolName+" "+portName);
		if (protocolName.equals("LocoNet LocoBuffer")) {
			//
			jmri.jmrix.loconet.locobuffer.LocoBufferAdapter a 
					= new jmri.jmrix.loconet.locobuffer.LocoBufferAdapter();
			a.openPort(portName, "DecoderPro");
			a.configure();
			
		} else if (protocolName.equals("LocoNet MS100")) {
			//
			jmri.jmrix.loconet.ms100.MS100Adapter a 
					= new jmri.jmrix.loconet.ms100.MS100Adapter();
			a.openPort(portName, "DecoderPro");
			a.configure();
			
		} else if (protocolName.equals("NCE")) {
			//
			jmri.jmrix.nce.serialdriver.SerialDriverAdapter a 
					= new jmri.jmrix.nce.serialdriver.SerialDriverAdapter();
			a.openPort(portName, "DecoderPro");
			a.configure();
			
		} else {
			// selected no match, so throw an error
			throw new jmri.JmriException();
		}
		
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
					log.info("LAF class set to "+e.getActionCommand());
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
	
	Element getGUI() {
		Element e = new Element("gui");
		String lafClassName = LAFGroup.getSelection().getActionCommand();
		
		e.addAttribute("LAFclass", lafClassName);
		return e;
	}

	boolean configureGUI(Element e) {
		String name = e.getAttribute("LAFclass").getValue();
        String className = (String) installedLAFs.get(name);
		log.debug("GUI selection: "+name+" class name: "+className);
		// show on button
		Enumeration enum = LAFGroup.getElements();
		while (enum.hasMoreElements()) {
			JRadioButton b = (JRadioButton)enum.nextElement();
			if (b.getLabel().equals(name)) b.setSelected(true);
		}
		// set the GUI
		log.debug("setting GUI");
        if (className != null) {
            try {
                updateLookAndFeel(name, className);
            } catch (Exception ex) {
                log.error("Exception while setting GUI look & feel: "+ex);
            }
        }
		return true;
	}

    /** 
     *  Change the look-and-feel to the specified class.
     *  Alert the user if there were problems loading the PLAF.
     *  @param name (String) the presentable name for the class
     *  @param className (String) the className to be fed to the UIManager
     *  @see javax.swing.UIManager#setLookAndFeel
     *  @see javax.swing.SwingUtilities#updateComponentTreeUI
     */ 
    public void updateLookAndFeel(String name, String className) {
	try {
            // Set the new look and feel, and update the sample message to reflect it.
	    	UIManager.setLookAndFeel(className);
            // Call for a UI refresh to the new LAF starting at the highest level
            SwingUtilities.updateComponentTreeUI(getContentPane());
        } catch (Exception e) {
            String errMsg = "The " + name + " look-and-feel ";
            if (e instanceof UnsupportedLookAndFeelException){
                errMsg += "is not supported on this platform.";
            } else if (e instanceof ClassNotFoundException){
                errMsg += "could not be found.";
            } else {
                errMsg += "could not be loaded.";
            }
            
            log.error(errMsg);
            
        }
    }
                
	JPanel createProgrammerPane() {
		JPanel j = new JPanel();
		j.setLayout(new BoxLayout(j, BoxLayout.Y_AXIS));
		return j;
	}
	
	boolean configureProgrammer(Element e) {
		return true;
	}

	
	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderProConfigFrame.class.getName());
		
}
