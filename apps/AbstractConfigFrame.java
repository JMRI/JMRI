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
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002. AC 11/09/2002 Added SPROG support
 * @version			$Revision: 1.25 $
 */
abstract public class AbstractConfigFrame extends JFrame {

    public AbstractConfigFrame(String name) {
        super(name);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        // create the GUI in steps
        addCommPane();
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

    protected void addCommPane() {
        commPane = new DefaultCommConfigPane(availableProtocols());
        getContentPane().add(commPane);
    }

    protected DefaultCommConfigPane commPane;

    public DefaultCommConfigPane getCommPane() { return commPane; }

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
     * <LI>"SPROG"
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
                              "NCE",
                              "SPROG"
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
        boolean connected = commPane.configureConnection(file.getConnectionElement());
        boolean gui = configureGUI(file.getGuiElement());
        boolean programmer = configureProgrammer(file.getProgrammerElement());

        // invoke an action (if element exists, etc) only if succeeded so far
        if (connected&&gui&&programmer) invokeAction(file.getPerformElement());

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

    protected boolean configureGUI(Element e) {
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

    protected boolean configureProgrammer(Element e) {
        jmri.jmrit.symbolicprog.CombinedLocoSelPane.setDefaultProgFile(e.getAttribute("defaultFile").getValue());
        programmerBox.setSelectedItem(e.getAttribute("defaultFile").getValue());
        return true;
    }

    protected void invokeAction(Element e) {
        if (e==null) return;    // no element, do nothing
        String className = "<unknown>";
        try {
            className = e.getAttribute("classname").getValue();
            Action action = (Action)Class.forName(className).newInstance();
            action.actionPerformed(new ActionEvent("prefs", 0, ""));
        } catch (ClassNotFoundException ex1) {
            log.error("Could not find specified class: "+className);
        } catch (IllegalAccessException ex2) {
            log.error("Unexpected access exception: "+ex2);
        } catch (InstantiationException ex3) {
            log.error("Could not instantiate specified class: "+className);
        } catch (Exception ex4) {
            log.error("Error while performing startup action: "+ex4);
            ex4.printStackTrace();
        }
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractConfigFrame.class.getName());

}

