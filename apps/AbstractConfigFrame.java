// AbstractConfigFrame.java

package apps;

import apps.AbstractConfigFile;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.jdom.Element;
import org.jdom.Attribute;

/**
 * AbstractConfigFrame provides startup configuration, a GUI for setting
 * config/preferences, and read/write support.   Note that routine GUI config,
 * menu building, etc is done in other code.
 *<P>For now, we're implicitly assuming that configuration of these
 * things is _only_ done here, so that we don't have to track anything
 * else.  When asked to write the config, we just write the values
 * stored in local variables.
 * <P>The protocol configuration here is done with cut and paste. There are
 * three places that a new protocol needs to be added:  (1) as a name in an array
 * (2) Updating options when the name is selected (3) Actually connecting to the port.
 *
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision: 1.18 $
 */
abstract public class AbstractConfigFrame extends JFrame {

    public AbstractConfigFrame(String name) {
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

        // add some space at the bottom
        getContentPane().add(new JLabel(" "));

        // show is deferred to some action somewhere else
        pack();
    }

    /**
     * Overload this to subset the list of available protocols
     * The supported list is:<UL>
     * <LI>"NCE"
     * <LI>"LocoNet LocoBuffer"
     * <LI>"LocoNet MS100"
     * <LI>"LocoNet Server"
     * <LI>"LocoNet HexFile"
     * <LI>"CMRI serial"
     * <LI>"EasyDCC"
     * <LI>"Lenz XPressNet"
     * </UL>
     * DecoderPro and JmriDemo are known to overload, hence may have to
     * be edited when this is changed.
     * @see apps.DecoderPro.DecoderProConfigFrame
     * @see apps.JmriDemo.JmriDemoConfigFrame
     * @return List of available protocols.
     */
    public String[] availableProtocols() {
        return  new String[] {"(None selected)",
                            "CMRI serial",
                            "EasyDCC",
                            "Lenz XPressNet",
                            "LocoNet LocoBuffer","LocoNet MS100",
                            "LocoNet Server", "LocoNet HexFile",
                            "NCE"
                        };
    }

    /**
     * Command reading the configuration, and setting it into the application.
     * Returns true if
     * a configuration file was found and loaded OK.
     * @param file Input configuration file
     * @throws jmri.JmriException from internal code
     * @return true if successful
     */
    public boolean configure(AbstractConfigFile file) throws jmri.JmriException {
        boolean connected = configureConnection(file.getConnectionElement());
        boolean gui = configureGUI(file.getGuiElement());
        boolean programmer = configureProgrammer(file.getProgrammerElement());
        return connected&&gui&&programmer;
    }

    /**
     * Abstract method to save the data
     */
    public abstract void saveContents();

    /**
     * Handle the Save button:  Backup the file, write a new one, prompt for
     * what to do next.  To do that, the last step is to present a dialog
     * box prompting the user to end the program.
     */
    public void savePressed() {
        jmri.jmrit.XmlFile.ensurePrefsPresent(jmri.jmrit.XmlFile.prefsDir());

        saveContents();

        if (JOptionPane.showConfirmDialog(null,
                "Your updated preferences will take effect when the program is restarted. Quit now?",
                "Quit now?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            // end the program
            setVisible(false);
            dispose();
            System.exit(0);
        }
        // don't end the program, just close the window
        setVisible(false);
    }

    JComboBox protocolBox;
    JComboBox portBox;
    JComboBox baudBox;
    JComboBox opt1Box;
    JComboBox opt2Box;

    /*
     * Create a panel showing the valid connection methods and port names
     */
    JPanel createConnectionPane() {
    	JPanel j = new JPanel();

    	JLabel l;

    	j.setLayout(new GridLayout(5,2));
    	protocolBox = new JComboBox(availableProtocols());
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

        baudBox = new JComboBox(new String[] {"(select a connection method first)"});
        baudBox.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                baudSelected();
            }
        });
        baudBox.setToolTipText("This is disabled until you select a connection method");
        baudBox.setEnabled(false);

        l = new JLabel("Baud rate: ");
        j.add(l);
        j.add(baudBox);

        opt1Box = new JComboBox(new String[] {"(select a connection method first)"});
        opt1Box.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                opt1Selected();
            }
        });
        opt1Box.setToolTipText("This is disabled until you select a connection method");
        opt1Box.setEnabled(false);

        l = new JLabel("Communications option: ");
        j.add(l);
        j.add(opt1Box);

        opt2Box = new JComboBox(new String[] {"(select a connection method first)"});
        opt2Box.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                opt2Selected();
            }
        });
        opt2Box.setToolTipText("This is disabled until you select a connection method");
        opt2Box.setEnabled(false);

        l = new JLabel("Command station option: ");
        j.add(l);
        j.add(opt2Box);

        return j;
    }

	/*
	 * Connection method has been selected; show available ports
	 */
	void protocolSelected() {
        try {
    		portBox.setEnabled(true);
            portBox.setEditable(false);
    		portBox.setToolTipText("Select a communications port");

    		// create the eventual serial driver object, and ask it for available comm ports
    		protocolName = (String) protocolBox.getSelectedItem();
    		portBox.removeAllItems();  // start over
    		baudBox.removeAllItems();  // start over
    		opt1Box.removeAllItems();  // start over
    		opt2Box.removeAllItems();  // start over

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
    			String[] baudList = a.validBaudRates();
    			for (int i=0; i<baudList.length; i++) baudBox.addItem(baudList[i]);
    			String[] opt1List = a.validOption1();
    			for (int i=0; i<opt1List.length; i++) opt1Box.addItem(opt1List[i]);
    			String[] opt2List = a.validOption2();
    			for (int i=0; i<opt2List.length; i++) opt2Box.addItem(opt2List[i]);

    			if (baudList.length>1) {
    				baudBox.setToolTipText("Must match the baud rate setting of your hardware");
    				baudBox.setEnabled(true);
    			} else {
    				baudBox.setToolTipText("The baud rate is fixed for this protocol");
    				baudBox.setEnabled(false);
    			}
    			if (opt1List.length>1) {
    				opt1Box.setToolTipText("The first option is strongly recommended. See README for more info.");
    				opt1Box.setEnabled(true);
    			} else {
    				opt1Box.setToolTipText("There are no options for this protocol");
    				opt1Box.setEnabled(false);
    			}
    			if (opt2List.length>1) {
    				opt2Box.setToolTipText("");
    				opt2Box.setEnabled(true);
    			} else {
    				opt2Box.setToolTipText("There are no options for this protocol");
    				opt2Box.setEnabled(false);
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
    			String[] baudList = a.validBaudRates();
    			for (int i=0; i<baudList.length; i++) baudBox.addItem(baudList[i]);
    			String[] opt1List = a.validOption1();
    			for (int i=0; i<opt1List.length; i++) opt1Box.addItem(opt1List[i]);
    			String[] opt2List = a.validOption2();
    			for (int i=0; i<opt2List.length; i++) opt2Box.addItem(opt2List[i]);

    			if (baudList.length>1) {
    				baudBox.setToolTipText("Must match the baud rate setting of your hardware");
    				baudBox.setEnabled(true);
    			} else {
    				baudBox.setToolTipText("The baud rate is fixed for this protocol");
    				baudBox.setEnabled(false);
    			}
    			if (opt1List.length>1) {
    				opt1Box.setToolTipText("The first option is strongly recommended. See README for more info.");
    				opt1Box.setEnabled(true);
    			} else {
    				opt1Box.setToolTipText("There are no options for this protocol");
    				opt1Box.setEnabled(false);
    			}
    			if (opt2List.length>1) {
    				opt2Box.setToolTipText("");
    				opt2Box.setEnabled(true);
    			} else {
    				opt2Box.setToolTipText("There are no options for this protocol");
    				opt2Box.setEnabled(false);
    			}

    		} else if (protocolName.equals("LocoNet HexFile")) {
    			//
    			log.debug("HexFile has no ports to find");

    			baudBox.setToolTipText("The baud rate is fixed for this protocol");
                baudBox.setEnabled(false);

                opt1Box.setToolTipText("There are no options for this protocol");
                opt1Box.setEnabled(false);

                opt2Box.setToolTipText("There are no options for this protocol");
                opt2Box.setEnabled(false);

    		} else if (protocolName.equals("LocoNet Server")) {
    			// This is somewhat special, as the option has to
                // allow editting in a host name
                portBox.setEditable(true);
    	    	portBox.setToolTipText("Enter a server hostname");

                baudBox.setToolTipText("Don't need to specify baud rate");
                baudBox.setEnabled(false);

        		opt1Box.setToolTipText("There are no options for this protocol");
        		opt1Box.setEnabled(false);
        		opt2Box.setToolTipText("There are no options for this protocol");
        		opt2Box.setEnabled(false);

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
    			String[] baudList = a.validBaudRates();
    			for (int i=0; i<baudList.length; i++) baudBox.addItem(baudList[i]);
    			String[] opt1List = a.validOption1();
    			for (int i=0; i<opt1List.length; i++) opt1Box.addItem(opt1List[i]);
    			String[] opt2List = a.validOption2();
    			for (int i=0; i<opt2List.length; i++) opt2Box.addItem(opt2List[i]);

    			if (baudList.length>1) {
    				baudBox.setToolTipText("Must match the baud rate setting of your hardware");
    				baudBox.setEnabled(true);
    			} else {
    				baudBox.setToolTipText("The baud rate is fixed for this protocol");
    				baudBox.setEnabled(false);
    			}
    			if (opt1List.length>1) {
    				opt1Box.setToolTipText("The first option is strongly recommended. See README for more info.");
    				opt1Box.setEnabled(true);
    			} else {
    				opt1Box.setToolTipText("There are no options for this protocol");
    				opt1Box.setEnabled(false);
    			}
    			if (opt2List.length>1) {
    				opt2Box.setToolTipText("");
    				opt2Box.setEnabled(true);
    			} else {
    				opt2Box.setToolTipText("There are no options for this protocol");
    				opt2Box.setEnabled(false);
    			}
    		} else if (protocolName.equals("EasyDCC")) {
    			//
    			jmri.jmrix.easydcc.serialdriver.SerialDriverAdapter a
    					= new jmri.jmrix.easydcc.serialdriver.SerialDriverAdapter();
    			Vector v = a.getPortNames();
    			log.debug("Found "+v.size()+" EasyDCC ports");
    			for (int i=0; i<v.size(); i++) {
    				if (i==0) portName = (String) v.elementAt(i);
    				portBox.addItem(v.elementAt(i));
    			}
    			String[] baudList = a.validBaudRates();
    			for (int i=0; i<baudList.length; i++) baudBox.addItem(baudList[i]);
    			String[] opt1List = a.validOption1();
    			for (int i=0; i<opt1List.length; i++) opt1Box.addItem(opt1List[i]);
    			String[] opt2List = a.validOption2();
    			for (int i=0; i<opt2List.length; i++) opt2Box.addItem(opt2List[i]);

    			if (baudList.length>1) {
    				baudBox.setToolTipText("Must match the baud rate setting of your hardware");
    				baudBox.setEnabled(true);
    			} else {
    				baudBox.setToolTipText("The baud rate is fixed for this protocol");
    				baudBox.setEnabled(false);
    			}
    			if (opt1List.length>1) {
    				opt1Box.setToolTipText("The first option is strongly recommended. See README for more info.");
    				opt1Box.setEnabled(true);
    			} else {
    				opt1Box.setToolTipText("There are no options for this protocol");
    				opt1Box.setEnabled(false);
    			}
    			if (opt2List.length>1) {
    				opt2Box.setToolTipText("");
    				opt2Box.setEnabled(true);
    			} else {
    				opt2Box.setToolTipText("There are no options for this protocol");
    				opt2Box.setEnabled(false);
    			}
    		} else if (protocolName.equals("Lenz XPressNet")) {
    			//
    			jmri.jmrix.lenz.li100.LI100Adapter a
    					= new jmri.jmrix.lenz.li100.LI100Adapter();
    			Vector v = a.getPortNames();
    			log.debug("Found "+v.size()+" XPressNet ports");
    			for (int i=0; i<v.size(); i++) {
    				if (i==0) portName = (String) v.elementAt(i);
    				portBox.addItem(v.elementAt(i));
    			}
    			String[] baudList = a.validBaudRates();
    			for (int i=0; i<baudList.length; i++) baudBox.addItem(baudList[i]);
    			String[] opt1List = a.validOption1();
    			for (int i=0; i<opt1List.length; i++) opt1Box.addItem(opt1List[i]);
    			String[] opt2List = a.validOption2();
    			for (int i=0; i<opt2List.length; i++) opt2Box.addItem(opt2List[i]);

    			if (baudList.length>1) {
    				baudBox.setToolTipText("Must match the baud rate setting of your hardware");
    				baudBox.setEnabled(true);
    			} else {
    				baudBox.setToolTipText("The baud rate is fixed for this protocol");
    				baudBox.setEnabled(false);
    			}
    			if (opt1List.length>1) {
    				opt1Box.setToolTipText("The first option is strongly recommended. See README for more info.");
    				opt1Box.setEnabled(true);
    			} else {
    				opt1Box.setToolTipText("There are no options for this protocol");
    				opt1Box.setEnabled(false);
    			}
    			if (opt2List.length>1) {
    				opt2Box.setToolTipText("");
    				opt2Box.setEnabled(true);
    			} else {
    				opt2Box.setToolTipText("There are no options for this protocol");
    				opt2Box.setEnabled(false);
    			}

    		} else if (protocolName.equals("CMRI serial")) {
    			//
    			jmri.jmrix.cmri.serial.serialdriver.SerialDriverAdapter a
    					= new jmri.jmrix.cmri.serial.serialdriver.SerialDriverAdapter();
    			Vector v = a.getPortNames();
    			log.debug("Found "+v.size()+" CMRI serial ports");
    			for (int i=0; i<v.size(); i++) {
    				if (i==0) portName = (String) v.elementAt(i);
    				portBox.addItem(v.elementAt(i));
    			}
    			String[] baudList = a.validBaudRates();
    			for (int i=0; i<baudList.length; i++) baudBox.addItem(baudList[i]);
    			String[] opt1List = a.validOption1();
    			for (int i=0; i<opt1List.length; i++) opt1Box.addItem(opt1List[i]);
    			String[] opt2List = a.validOption2();
    			for (int i=0; i<opt2List.length; i++) opt2Box.addItem(opt2List[i]);

    			if (baudList.length>1) {
    				baudBox.setToolTipText("Must match the baud rate setting of your hardware");
    				baudBox.setEnabled(true);
    			} else {
    				baudBox.setToolTipText("The baud rate is fixed for this protocol");
    				baudBox.setEnabled(false);
    			}
    			if (opt1List.length>1) {
    				opt1Box.setToolTipText("The first option is strongly recommended. See README for more info.");
    				opt1Box.setEnabled(true);
    			} else {
    				opt1Box.setToolTipText("There are no options for this protocol");
    				opt1Box.setEnabled(false);
    			}
    			if (opt2List.length>1) {
    				opt2Box.setToolTipText("");
    				opt2Box.setEnabled(true);
    			} else {
    				opt2Box.setToolTipText("There are no options for this protocol");
    				opt2Box.setEnabled(false);
    			}
    		} else {
	    		// selected nothing, so put it back as it was
	    			portBox.addItem("(select a connection method first)");
	    			portBox.setToolTipText("This is disabled until you select a connection method");
	    			portBox.setEnabled(false);
	    			baudBox.addItem("(select a connection method first)");
	    			baudBox.setToolTipText("This is disabled until you select a connection method");
	    			baudBox.setEnabled(false);
	    			opt1Box.addItem("(select a connection method first)");
	    			opt1Box.setToolTipText("This is disabled until you select a connection method");
	    			opt1Box.setEnabled(false);
	    			opt2Box.addItem("(select a connection method first)");
	    			opt2Box.setToolTipText("This is disabled until you select a connection method");
	    			opt2Box.setEnabled(false);
	    	}
	    }
        catch (java.lang.NoClassDefFoundError e) {
		    JOptionPane.showMessageDialog(null,
		   	    "There was a problem loading the communications library, and the program "+
                "cannot continue. Check that the library was installed correctly.",
		        "Error - program will quit",
                JOptionPane.ERROR_MESSAGE);
            // end the program
            setVisible(false);
            System.exit(0);
        }
    }

    /*
     * Port name has been selected; store
     */
    void portSelected() {
        portName = (String) portBox.getSelectedItem();
    }

    /*
     * Baud rate has been selected; store
     */
    void baudSelected() {
        baudRate = (String) baudBox.getSelectedItem();
    }

    /*
     * Option1 value has been selected; store
     */
    void opt1Selected() {
        option1Setting = (String) opt1Box.getSelectedItem();
    }

    /*
     * Option2 value has been selected; store
     */
    void opt2Selected() {
        option2Setting = (String) opt2Box.getSelectedItem();
    }

    jmri.jmrix.SerialPortAdapter port = null;
    String protocolName = "(None selected)";
    String portName = "(None selected)";
    String baudRate = "(None selected)";
    String option1Setting = "(None selected)";
    String option2Setting = "(None selected)";

    public Element getConnection() {
        Element e = new Element("connection");
        // many of the following are required by the DTD; failing to include
        // them makes the XML file unreadable, but at least the next
        // invocation of the program can then continue.
        if (getCurrentProtocolName()!=null)
            e.addAttribute("class", getCurrentProtocolName());

        if (getCurrentPortName()!=null)
            e.addAttribute("port", getCurrentPortName());
        else e.addAttribute("port", "(None selected)");

        if (getCurrentBaudRate()!=null)
            e.addAttribute("speed", getCurrentBaudRate());
        else e.addAttribute("speed", "(None selected)");

        if (getCurrentOption1Setting()!=null)
            e.addAttribute("option1", getCurrentOption1Setting());
        else e.addAttribute("option1", "(None selected)");

        if (getCurrentOption2Setting()!=null)
            e.addAttribute("option2", getCurrentOption2Setting());
        else e.addAttribute("option2", "(None selected)");

        return e;
    }

    public String getCurrentProtocolName() { return protocolName; }
    public String getCurrentPortName() { return portName; }
    public String getCurrentBaudRate() { return baudRate; }
    public String getCurrentOption1Setting() { return option1Setting; }
    public String getCurrentOption2Setting() { return option2Setting; }

    boolean configureConnection(Element e) throws jmri.JmriException {
        protocolName = e.getAttribute("class").getValue();

        // check that the config file has a protocol selected
        if (protocolName.equals("(None selected)")) return false;

        protocolBox.setSelectedItem(protocolName);
        // note that the line above will _change_ the value of portName, as it
        // selects a default and runs the GUI config for that protocol

        // configure port name
        portName = e.getAttribute("port").getValue();
        // ugly special-case hack: skip parts for certain protocols
        if (!protocolName.equals("LocoNet HexFile")) {
            portBox.setSelectedItem(e.getAttribute("port").getValue());
            portName = e.getAttribute("port").getValue();  // may have been changed by prior line
            // check that the specified port exists
            if (!e.getAttribute("port").getValue().equals(portBox.getSelectedItem())) {
                // can't connect to a non-existant port!
                log.error("Configured port \""+portName+"\" doesn't exist, no connection to layout made");
                JOptionPane.showMessageDialog(null, "Configured port \""+portName+"\" doesn't exist, no connection to layout made",
                                                "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if (!protocolName.equals("LocoNet Server")) {
                // configure baud rate - an optional attribute
                if (e.getAttribute("speed")!=null) {
                    baudRate = e.getAttribute("speed").getValue();
                    baudBox.setSelectedItem(baudRate);
                    baudRate = e.getAttribute("speed").getValue();  // may have been changed by prior line
                    // check that the specified setting exists
                    if (!e.getAttribute("speed").getValue().equals(baudBox.getSelectedItem())) {
                        // can't set non-existant option value!
                        log.error("Configured baud rate\""+baudRate+"\" doesn't exist, no connection to layout made");
                        return false;
                    }
                }

                // configure option1 - an optional attribute
                if (e.getAttribute("option1")!=null) {
                    option1Setting = e.getAttribute("option1").getValue();
                    opt1Box.setSelectedItem(option1Setting);
                    option1Setting = e.getAttribute("option1").getValue();  // may have been changed by prior line
                    // check that the specified setting exists
                    if (!e.getAttribute("option1").getValue().equals(opt1Box.getSelectedItem())) {
                        // can't set non-existant option value!
                        log.error("Configured option1 value \""+option1Setting+"\" doesn't exist, no connection to layout made");
                        return false;
                    }
                }

                // configure option2 - an optional attribute
                if (e.getAttribute("option2")!=null) {
                    option2Setting = e.getAttribute("option2").getValue();
                    opt2Box.setSelectedItem(option2Setting);
                    option2Setting = e.getAttribute("option2").getValue();  // may have been changed by prior line
                    // check that the specified setting exists
                    if (!e.getAttribute("option2").getValue().equals(opt2Box.getSelectedItem())) {
                        // can't set non-existant option value!
                        log.error("Configured option2 value \""+option1Setting+"\" doesn't exist, no connection to layout made");
                        return false;
                    }
                }
            }
        } // end of HexFile special-case hack

        // handle the specific case (a good use for reflection!)
        log.info("Configuring connection with "+protocolName+" "+portName);
        if (protocolName.equals("LocoNet LocoBuffer")) {
            //
            jmri.jmrix.loconet.locobuffer.LocoBufferAdapter a
                = new jmri.jmrix.loconet.locobuffer.LocoBufferAdapter();
            a.configureBaudRate(getCurrentBaudRate());
            a.configureOption1(getCurrentOption1Setting());
            a.configureOption2(getCurrentOption2Setting());
            a.openPort(portName, "JMRI/DecoderPro");
            a.configure();
            if (!a.okToSend()) {
                log.info("LocoBuffer port not ready to send");
                JOptionPane.showMessageDialog(null,
                        "The LocoBuffer is unable to accept data.\n"
                        +"Make sure its power is on, it is connected\n"
                        +"to a working LocoNet, and the command station is on.\n"
                        +"The LocoNet LED on the LocoBuffer should be off.\n"
                        +"Reset the LocoBuffer by cycling its power.",
                        "LocoBuffer not ready", JOptionPane.ERROR_MESSAGE);
            }

        } else if (protocolName.equals("LocoNet MS100")) {
            //
            jmri.jmrix.loconet.ms100.MS100Adapter a
                = new jmri.jmrix.loconet.ms100.MS100Adapter();
            a.openPort(portName, "JMRI/DecoderPro");
            a.configure();
            a.configureOption2(getCurrentOption2Setting());

        } else if (protocolName.equals("LocoNet HexFile")) {
            // pop the panel
            jmri.jmrix.loconet.hexfile.HexFileFrame f
                = new jmri.jmrix.loconet.hexfile.HexFileFrame();
            try {
                f.initComponents();
            } catch (Exception ex) {
                log.error("starting HexFileFrame exception: "+ex.toString());
            }
            f.pack();
            f.show();

        } else if (protocolName.equals("LocoNet Server")) {
            // slightly different, as not based on a serial port...
            // create the LnMessageClient
            jmri.jmrix.loconet.locormi.LnMessageClient client = new jmri.jmrix.loconet.locormi.LnMessageClient();

            // start the connection
            client.configureRemoteConnection(portName, 500);

            // configure the other instance objects
            client.configureLocalServices();

        } else if (protocolName.equals("NCE")) {
            //
            jmri.jmrix.nce.serialdriver.SerialDriverAdapter a
                = new jmri.jmrix.nce.serialdriver.SerialDriverAdapter();
            a.openPort(portName, "JMRI/DecoderPro");
            a.configure();

        } else if (protocolName.equals("EasyDCC")) {
            //
            jmri.jmrix.easydcc.serialdriver.SerialDriverAdapter a
                = new jmri.jmrix.easydcc.serialdriver.SerialDriverAdapter();
            a.openPort(portName, "JMRI/DecoderPro");
            a.configure();

        } else if (protocolName.equals("Lenz XPressNet")) {
            //
            jmri.jmrix.lenz.li100.LI100Adapter a
                = new jmri.jmrix.lenz.li100.LI100Adapter();
            a.configureBaudRate(getCurrentBaudRate());
            a.configureOption1(getCurrentOption1Setting());
            a.openPort(portName, "JMRI/DecoderPro");
            a.configure();

        } else if (protocolName.equals("CMRI serial")) {
            //
            jmri.jmrix.cmri.serial.serialdriver.SerialDriverAdapter a
                = new jmri.jmrix.cmri.serial.serialdriver.SerialDriverAdapter();
            a.openPort(portName, "JMRI/DecoderPro");
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

        c.add(new JLabel("GUI style: "));
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

    public Element getGUI() {
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
            if (b.getText().equals(name)) b.setSelected(true);
        }
        // set the GUI
        if (className != null) {
            try {
                if (!className.equals(UIManager.getLookAndFeel().getClass().getName())) {
                    log.debug("set GUI to "+name+","+className);
                    updateLookAndFeel(name, className);
                } else
                    log.debug("skip updateLAF as already has className=="+className);
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

    JComboBox programmerBox = null;

	JPanel createProgrammerPane() {
		JPanel j = new JPanel();
		j.setLayout(new BoxLayout(j, BoxLayout.X_AXIS));
		j.add(new JLabel("Default programmer format: "));
		j.add(programmerBox = new JComboBox(jmri.jmrit.symbolicprog.CombinedLocoSelPane.findListOfProgFiles()));
		return j;
	}

	public Element getProgrammer() {
		Element programmer = new Element("programmer");
		programmer.addAttribute("defaultFile", (String)programmerBox.getSelectedItem());
		programmer.addAttribute("verifyBeforeWrite", "no");
		return programmer;
	}

	boolean configureProgrammer(Element e) {
		jmri.jmrit.symbolicprog.CombinedLocoSelPane.setDefaultProgFile(e.getAttribute("defaultFile").getValue());
		programmerBox.setSelectedItem(e.getAttribute("defaultFile").getValue());
		return true;
	}


	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractConfigFrame.class.getName());

}
