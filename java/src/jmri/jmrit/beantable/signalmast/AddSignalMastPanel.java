package jmri.jmrit.beantable.signalmast;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import jmri.*;
import jmri.implementation.*;
import jmri.util.*;
import jmri.util.swing.*;

import org.jdom2.Element;

/**
 * JPanel to create a new Signal Mast.
 * 
 * "Driver" refers to a particular class of SignalMast implementation that's to be configured.
 *
 * @author Bob Jacobsen Copyright (C) 2009, 2010, 2016
 * @author Egbert Broerse Copyright (C) 2016
 */
public class AddSignalMastPanel extends JPanel {
    
    // head matter
    JTextField userName = new JTextField(20);
    JComboBox<String> sigSysBox = new JComboBox<>();  // the basic signal system
    JComboBox<String> mastBox = new JComboBox<>(new String[]{Bundle.getMessage("MastEmpty")}); // the mast within the system NOI18N
    boolean mastBoxPassive = false; // if true, mastBox doesn't process updates
    JComboBox<String> signalMastDriver;   // the specific SignalMast class type

    List<SignalMastAddPane> panes = new ArrayList<>();

    // center pane, which holds the specific display
    JPanel centerPanel = new JPanel();
    CardLayout cl = new CardLayout();
    SignalMastAddPane currentPane;
    
    // rest of structure
    JPanel signalHeadPanel = new JPanel();
    JButton cancel = new JButton(Bundle.getMessage("ButtonCancel")); // NOI18N
    JButton apply = new JButton(Bundle.getMessage("ButtonApply")); // NOI18N
    JButton create = new JButton(Bundle.getMessage("ButtonCreate")); // NOI18N

    // connection to preferences
    jmri.UserPreferencesManager prefs = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
    String systemSelectionCombo = this.getClass().getName() + ".SignallingSystemSelected"; // NOI18N
    String mastSelectionCombo = this.getClass().getName() + ".SignallingMastSelected"; // NOI18N
    String driverSelectionCombo = this.getClass().getName() + ".SignallingDriverSelected"; // NOI18N

    // current mast being worked on
    SignalMast mast;
    
    /**
     * Constructor providing a blank panel to configure a new signal mast after
     * pressing 'Add...' on the Signal Mast Table.
     * <p>
     * Responds to choice of signal system, mast type and driver
     * {@link #updateSelectedDriver()}
     */
    public AddSignalMastPanel() {
        log.debug("AddSignalMastPanel()");
        // get the list of possible signal types (as shown by panes)
        SignalMastAddPane.SignalMastAddPaneProvider.getInstancesCollection().forEach(
            (provider)-> {
                if (provider.isAvailable()) {
                    panes.add(provider.getNewPane());
                }
            }
        );
        
        { // scoping for temporary variables

            String[] tempMastNamesArray = new String[panes.size()];
            int i = 0;
            for (SignalMastAddPane pane : panes) {
                tempMastNamesArray[i++] = pane.getPaneName();
            }
            signalMastDriver = new JComboBox<>(tempMastNamesArray);
        }

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p;
        p = new JPanel();
        p.setLayout(new jmri.util.javaworld.GridLayout2(5, 2));

        JLabel l = new JLabel(Bundle.getMessage("LabelUserName"));  // NOI18N
        p.add(l);
        p.add(userName);

        l = new JLabel(Bundle.getMessage("SigSys") + ": "); // NOI18N
        p.add(l);
        p.add(sigSysBox);

        l = new JLabel(Bundle.getMessage("MastType") + ": "); // NOI18N
        p.add(l);
        p.add(mastBox);

        l = new JLabel(Bundle.getMessage("DriverType") + ": "); // NOI18N
        p.add(l);
        p.add(signalMastDriver);

        add(p);

        // central region
        centerPanel.setLayout(cl);
        for (SignalMastAddPane pane : panes) {
            centerPanel.add(pane, pane.getPaneName()); // assumes names are systemwide-unique
        }
        add(centerPanel);
        signalMastDriver.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent evt) {
                    log.trace("about to call selection() from signalMastDriver itemStateChanged");
                    selection((String)evt.getItem());
                }
        });
        
        // button region
        JPanel buttonHolder = new JPanel();
        buttonHolder.setLayout(new FlowLayout(FlowLayout.TRAILING));
        cancel.setVisible(true);
        buttonHolder.add(cancel);
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelPressed();
            } // Cancel button
        });
        cancel.setVisible(true);
        buttonHolder.add(create);
        create.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okPressed();
            } // Create button on add new mast pane
        });
        create.setVisible(true);
        buttonHolder.add(apply);
        apply.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okPressed();
            } // Apply button on Edit existing mast pane
        });
        apply.setVisible(false);
        add(buttonHolder); // add bottom row of buttons (to me)

        // default to 1st pane
        currentPane = panes.get(0);
        
        // load the list of signal systems
        SignalSystemManager man = InstanceManager.getDefault(SignalSystemManager.class);
        SortedSet<SignalSystem> systems = man.getNamedBeanSet();
        for (SignalSystem system : systems) {
            sigSysBox.addItem(system.getUserName());
        }
        if (prefs.getComboBoxLastSelection(systemSelectionCombo) != null) {
            sigSysBox.setSelectedItem(prefs.getComboBoxLastSelection(systemSelectionCombo));
        }
        log.trace("  preferences set {} into sigSysBox", sigSysBox.getSelectedItem());
        
        loadMastDefinitions();

        // select the 1st one
        selection(panes.get(0).getPaneName());  // there has to be at least one, so we can do the update

        // set a remembered signalmast type, if present
        if (prefs.getComboBoxLastSelection(driverSelectionCombo) != null) {
            signalMastDriver.setSelectedItem(prefs.getComboBoxLastSelection(driverSelectionCombo));
        }
        
        sigSysBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                loadMastDefinitions();
                updateSelectedDriver();
            }
        });
    }    

    /**
     * Select a particular signal implementation to display.
     */
    void selection(String view) {
        log.trace(" selection({}) start", view);
        // find the new pane
        for (SignalMastAddPane pane : panes) {
            if (pane.getPaneName().equals(view)) {
                currentPane = pane;
            }
        }
        
        // update that selected pane before display.
        updateSelectedDriver();
        
        // and show
        cl.show(centerPanel, view);
        log.trace(" selection({}) end", view);
    }

    /**
     * Build a panel filled in for existing mast after pressing 'Edit' in the
     * Signal Mast table.
     *
     * @param mast {@code NamedBeanHandle<SignalMast> } for the signal mast to
     *             be retrieved
     * @see #AddSignalMastPanel()
     */
    public AddSignalMastPanel(SignalMast mast) {
        this(); // calls the above method to build the base for an edit panel
        log.debug("AddSignalMastPanel({}) start", mast);

        // switch buttons
        apply.setVisible(true);
        create.setVisible(false);
        
        this.mast = mast;
        
        // can't change some things from original settings
        sigSysBox.setEnabled(false);
        mastBox.setEnabled(false);
        signalMastDriver.setEnabled(false);
        userName.setEnabled(false);
       
        //load prior content
        userName.setText(mast.getUserName()); 
        log.trace("Prior content system name: {}  mast type: {}", mast.getSignalSystem().getUserName(), mast.getMastType());
        if (mast.getMastType() == null) log.error("MastType was null, and never should be");
        sigSysBox.setSelectedItem(mast.getSignalSystem().getUserName());  // signal system
                
        // select and show
        for (SignalMastAddPane pane : panes) {
            if (pane.canHandleMast(mast)) {
                currentPane = pane;
                // set the driver combobox
                signalMastDriver.setSelectedItem(pane.getPaneName());
                log.trace("About to call selection() from SignalMastAddPane loop in AddSignalMastPanel(SignalMast mast)");
                selection(pane.getPaneName());

                // Ensure that the mast type is set
                mastBoxPassive = false;
                if (mapTypeToName.get(mast.getMastType()) == null ) {
                    log.error("About to set mast to null, which shouldn't happen. mast.getMastType() is {}", mast.getMastType(),
                            new Exception("Traceback Exception")); // NOI18N
                }
                log.trace("set mastBox to \"{}\" from \"{}\"", mapTypeToName.get(mast.getMastType()), mast.getMastType()); // NOI18N
                mastBox.setSelectedItem(mapTypeToName.get(mast.getMastType()));

                pane.setMast(mast);
                break;
            }
        }
        
        // set mast type, suppress notification
        mastBoxPassive = true;
        String newMastType = mapTypeToName.get(mast.getMastType());
        log.debug("Setting type to {}", newMastType); // NOI18N
        mastBox.setSelectedItem(newMastType);
        mastBoxPassive = false;

        log.debug("AddSignalMastPanel({}) end", mast);
    }
    
    // signal system definition variables
    String sigsysname;
    ArrayList<File> mastFiles = new ArrayList<>(); // signal system definition files
    LinkedHashMap<String, Integer> mapNameToShowSize = new LinkedHashMap<>();
    LinkedHashMap<String, String> mapTypeToName = new LinkedHashMap<>();

    /**
     * Load the mast definitions from the selected signal system.
     */
    void loadMastDefinitions() {
        log.trace(" loadMastDefinitions() start");
        // need to remove itemListener before addItem() or item event will occur
        if (mastBox.getItemListeners().length > 0) { // should this be a while loop?
            mastBox.removeItemListener(mastBox.getItemListeners()[0]);
        }
        mastBox.removeAllItems();
        try {
            mastFiles = new ArrayList<>();
            SignalSystemManager man = InstanceManager.getDefault(jmri.SignalSystemManager.class);

            // get the signals system name from the user name in combo box
            String u = (String) sigSysBox.getSelectedItem();
            sigsysname = man.getByUserName(u).getSystemName();
            log.trace("     loadMastDefinitions with sigsysname {}", sigsysname); // NOI18N
            mapNameToShowSize = new LinkedHashMap<>();
            mapTypeToName = new LinkedHashMap<>();

            // do file IO to get all the appearances
            // gather all the appearance files
            // Look for the default system defined ones first
            File[] programDirArray = new File[0];
            URL pathProgramDir = FileUtil.findURL("xml/signals/" + sigsysname, FileUtil.Location.INSTALLED); // NOI18N
            if (pathProgramDir != null) programDirArray = new File(pathProgramDir.toURI()).listFiles();
            if (programDirArray == null) programDirArray = new File[0];

            File[] profileDirArray = new File[0];
            URL pathProfileDir = FileUtil.findURL("resources/signals/" + sigsysname, FileUtil.Location.USER); // NOI18N
            if (pathProfileDir != null) profileDirArray = new File(pathProfileDir.toURI()).listFiles();
            if (profileDirArray == null) profileDirArray = new File[0];
            
            // create a composite list of files
            File[] apps = Arrays.copyOf(programDirArray, programDirArray.length + profileDirArray.length);
            System.arraycopy(profileDirArray, 0, apps, programDirArray.length, profileDirArray.length);
            
            if (apps !=null) {
                for (File app : apps) {
                    if (app.getName().startsWith("appearance") && app.getName().endsWith(".xml")) { // NOI18N
                        log.debug("   found file: {}", app.getName()); // NOI18N
                        // load it and get name
                        mastFiles.add(app);
                        jmri.jmrit.XmlFile xf = new jmri.jmrit.XmlFile() {
                        };
                        Element root = xf.rootFromFile(app);
                        String name = root.getChild("name").getText();
                        log.trace("mastNames adding \"{}\" mastBox adding \"{}\" ", app, name); // NOI18N
                        mastBox.addItem(name);
                        log.trace("mapTypeToName adding key \"{}\" value \"{}\"", app.getName().substring(11, app.getName().indexOf(".xml")), name); // NOI18N
                        mapTypeToName.put(app.getName().substring(11, app.getName().indexOf(".xml")), name); // NOI18N
                        mapNameToShowSize.put(name, root.getChild("appearances") // NOI18N
                                .getChild("appearance") // NOI18N
                                .getChildren("show") // NOI18N
                                .size());
                        
                    }
                }
            } else {
                log.error("Unexpected null list of signal definition files"); // NOI18N
            }

        } catch (org.jdom2.JDOMException e) {
            mastBox.addItem(Bundle.getMessage("ErrorSignalMastBox1")); // NOI18N
            log.warn("in loadMastDefinitions", e); // NOI18N
        } catch (java.io.IOException | URISyntaxException e) {
            mastBox.addItem(Bundle.getMessage("ErrorSignalMastBox2")); // NOI18N
            log.warn("in loadMastDefinitions", e); // NOI18N
        }

        try {
            URL path = FileUtil.findURL("signals/" + sigsysname, FileUtil.Location.USER, "xml", "resources"); // NOI18N
            if (path != null) {
                File[] apps = new File(path.toURI()).listFiles();
                if (apps != null) {
                    for (File app : apps) {
                        if (app.getName().startsWith("appearance") && app.getName().endsWith(".xml")) { // NOI18N
                            log.debug("   found file: {}", app.getName()); // NOI18N
                            // load it and get name
                            // If the mast file name already exists no point in re-adding it
                            if (!mastFiles.contains(app)) {
                                mastFiles.add(app);
                                jmri.jmrit.XmlFile xf = new jmri.jmrit.XmlFile() {
                                };
                                Element root = xf.rootFromFile(app);
                                String name = root.getChild("name").getText();
                                //if the mast name already exist no point in readding it.
                                if (!mapNameToShowSize.containsKey(name)) {
                                    mastBox.addItem(name);
                                    mapNameToShowSize.put(name, root.getChild("appearances") // NOI18N
                                            .getChild("appearance") // NOI18N
                                            .getChildren("show") // NOI18N
                                            .size());
                                }
                            }
                        }
                    }
                } else {
                    log.warn("No mast definition files found");
                }
            }
        } catch (org.jdom2.JDOMException | java.io.IOException | URISyntaxException e) {
            log.warn("in loadMastDefinitions", e); // NOI18N
        }
        mastBox.addItemListener((ItemEvent e) -> {
            if (!mastBoxPassive) updateSelectedDriver();
        });
        updateSelectedDriver();

        if (prefs.getComboBoxLastSelection(mastSelectionCombo + ":" + ((String) sigSysBox.getSelectedItem())) != null) { // NOI18N
            mastBox.setSelectedItem(prefs.getComboBoxLastSelection(mastSelectionCombo + ":" + ((String) sigSysBox.getSelectedItem())));
        }
        log.trace(" loadMastDefinitions() end");
    }

    /**
     * Update contents of Add/Edit mast panel appropriate for chosen Driver
     * type.
     * <p>
     * Invoked when selecting a Signal Mast Driver in {@link #loadMastDefinitions}
     */
    protected void updateSelectedDriver() {
        log.trace(" updateSelectedDriver() start");

        if (mastBox.getSelectedIndex() < 0) return; // no mast selected yet
        String mastFile = mastFiles.get(mastBox.getSelectedIndex()).getName();
        String mastType = mastFile.substring(11, mastFile.indexOf(".xml"));
        DefaultSignalAppearanceMap sigMap = DefaultSignalAppearanceMap.getMap(sigsysname, mastType);
        SignalSystem sigsys = InstanceManager.getDefault(jmri.SignalSystemManager.class).getSystem(sigsysname);
        currentPane.setAspectNames(sigMap, sigsys);
        // clear mast info
        currentPane.setMast(null);

        currentPane.revalidate();

        java.awt.Container ancestor = getTopLevelAncestor();
        if ((ancestor instanceof JmriJFrame)) {
            ((JmriJFrame) ancestor).pack();
        } else {
            log.debug("Can't call pack() on {}", ancestor);
        }
        log.trace(" updateSelectedDriver() end");
    }

    /**
     * Check of user name done when creating new SignalMast.
     * In case of error, it looks a message and (if not headless) shows a dialog.
     *
     * @return true if OK to proceed
     */
    boolean checkUserName(String nam) {
        if (!((nam == null) || (nam.equals("")))) {
            // user name provided, check if that name already exists
            NamedBean nB = InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName(nam);
            if (nB != null) {
                issueWarningUserName(nam);
                return false;
            }
            // Check to ensure that the username doesn't exist as a systemname.
            nB = InstanceManager.getDefault(jmri.SignalMastManager.class).getBySystemName(nam);
            if (nB != null) {
                issueWarningUserNameAsSystem(nam);
                return false;
            }
        }
        return true;
    }

    void issueWarningUserName(String nam) {
        log.error("User Name \"{}\" is already in use", nam); // NOI18N
        if (!GraphicsEnvironment.isHeadless()) {
            String msg = Bundle.getMessage("WarningUserName", new Object[]{("" + nam)}); // NOI18N
            JOptionPane.showMessageDialog(null, msg,
                    Bundle.getMessage("WarningTitle"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    void issueWarningUserNameAsSystem(String nam) {
        log.error("User Name \"{}\" already exists as a System name", nam);
        if (!GraphicsEnvironment.isHeadless()) {
            String msg = Bundle.getMessage("WarningUserNameAsSystem", new Object[]{("" + nam)});
            JOptionPane.showMessageDialog(null, msg,
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Store user input for a signal mast definition in new or existing mast
     * object.
     * <p>
     * Invoked from Apply/Create button.
     */
    private void okPressed() {
        log.trace(" okPressed() start");
        boolean success = false;
        
        // get and validate entered global information 
        if ( (mastBox.getSelectedIndex() < 0) || ( mastFiles.get(mastBox.getSelectedIndex()) == null) ) {
            issueDialogFailMessage(new RuntimeException("There's something wrong with the mast type selection"));
            return;
        }
        String mastname = mastFiles.get(mastBox.getSelectedIndex()).getName();
        String user = (userName.getText() != null ? NamedBean.normalizeUserName(userName.getText()) : ""); // NOI18N
        if (!GraphicsEnvironment.isHeadless()) {
            if (user == null || user.isEmpty()) {
                int i = issueNoUserNameGiven();
                if (i != 0) {
                    return;
                }
            }
        }
        
        // ask top-most pane to make a signal
        try {
            success = currentPane.createMast(sigsysname, mastname, user);
        } catch (RuntimeException ex) {
            issueDialogFailMessage(ex);
            return; // without clearing the panel, so user can try again
        }
        if (!success) {
            // should have already provided user feedback via dialog
            return;
        }
        
        clearPanel();
        log.trace(" okPressed() end");
    }

    int issueNoUserNameGiven() {
        return JOptionPane.showConfirmDialog(null, "No Username has been defined, this may cause issues when editing the mast later.\nAre you sure that you want to continue?",  // NOI18N
                "No UserName Given",  // NOI18N
                JOptionPane.YES_NO_OPTION);
    }
    
    void issueDialogFailMessage(RuntimeException ex) {
        // This is intrinsically swing, so pop a dialog
        log.error("Failed during createMast", ex); // NOI18N
        JOptionPane.showMessageDialog(this,
            Bundle.getMessage("DialogFailMessage", ex.toString()), // NOI18N
            Bundle.getMessage("DialogFailTitle"),  // title of box // NOI18N
            JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Called when an already-initialized AddSignalMastPanel is being
     * displayed again, right before it's set visible.
     */
    public void refresh() {
        log.trace(" refresh() start");
        // add new cards (new panes)
        centerPanel.removeAll();
        for (SignalMastAddPane pane : panes) {
            centerPanel.add(pane, pane.getPaneName()); // assumes names are systemwide-unique
        }
        
        // select pane to match current combobox
        log.trace("about to call selection from refresh");
        selection(signalMastDriver.getItemAt(signalMastDriver.getSelectedIndex()));
        log.trace(" refresh() end");
    }

    /**
     * Respond to the Cancel button.
     */
    private void cancelPressed() {
        log.trace(" cancelPressed() start");
        clearPanel();
        log.trace(" cancelPressed() end");
    }

    /**
     * Close and dispose() panel.
     * <p>
     * Called at end of okPressed() and from Cancel
     */
    private void clearPanel() {
        log.trace(" clearPanel() start");
        java.awt.Container ancestor = getTopLevelAncestor();
        if ((ancestor instanceof JmriJFrame)) {
            ((JmriJFrame) ancestor).dispose();
        } else {
            log.warn("Unexpected top level ancestor: {}", ancestor); // NOI18N
        }
        userName.setText(""); // clear user name
        log.trace(" clearPanel() end");
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AddSignalMastPanel.class);

}
