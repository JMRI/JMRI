package jmri.jmrit.throttle.implementation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.*;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.ThrottleManager;
import jmri.configurexml.StoreXmlConfigAction;
import jmri.jmrit.XmlFile;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.throttle.ThrottleFrameManager;
import jmri.jmrit.throttle.interfaces.AddressListener;
import jmri.jmrit.throttle.interfaces.ThrottleControllerUI;
import jmri.jmrit.throttle.panels.AddressPanel;
import jmri.jmrit.throttle.panels.BackgroundPanel;
import jmri.jmrit.throttle.panels.ConsistFunctionPanel;
import jmri.jmrit.throttle.panels.ControlPanel;
import jmri.jmrit.throttle.panels.FunctionPanel;
import jmri.jmrit.throttle.panels.SpeedPanel;
import jmri.jmrit.throttle.panels.LocoIconPanel;
import jmri.jmrit.throttle.preferences.ThrottlesPreferences;
import jmri.util.FileUtil;
import jmri.util.swing.JmriJOptionPane;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 * 
 * Inner class of a throttle UI holding most of the logic.
 * Used by classes actually implementing a throttle view (ThrottleFrame, SimpleThrottlePanel, ConsistFunctionPanel)
 * 
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Glen Oberhauser
 * @author Andrew Berridge Copyright 2010
 * @author Lionel Jeanson 2026
 * 
 */

public class ThrottleUICore implements AddressListener  {

    private DccThrottle throttle;
    private final ThrottleManager throttleManager;
    private final ThrottleFrameManager throttleFrameManager = InstanceManager.getDefault(ThrottleFrameManager.class);
    private final ThrottleControllerUI myThrottleController;
    private final boolean withPopupMenu; // should panel show the contextual menu that enable customization

    private ControlPanel controlPanel;
    private FunctionPanel functionPanel;
    private AddressPanel addressPanel;
    private BackgroundPanel backgroundPanel;
    private SpeedPanel speedPanel;
    private LocoIconPanel locoIconPanel;
    private ConsistFunctionPanel consistFunctionsPanel;

    private String lastUsedSaveFile = null;

    private static final String DEFAULT_THROTTLE_FILENAME = "JMRI_ThrottlePreference.xml";

    public static String getDefaultThrottleFolder() {
        return FileUtil.getUserFilesPath() + "throttle" + File.separator;
    }

    public static String getDefaultThrottleFilename() {
        return getDefaultThrottleFolder() + DEFAULT_THROTTLE_FILENAME;
    }

    public ThrottleUICore(ThrottleManager tm, ThrottleControllerUI tc, boolean withPopupMenu) {
        super();
        throttleManager = tm;
        myThrottleController = tc;
        this.withPopupMenu = withPopupMenu;
        initGUI();
    }

    public ThrottleUICore(ThrottleManager tm, ThrottleControllerUI tc) {
        this(tm,tc,true);
    }

    public AddressPanel getAddressPanel() {
        return addressPanel;
    }

    public DccThrottle getThrottle() {
        return getAddressPanel().getThrottle();  
    }

    public DccThrottle getFunctionThrottle() {
        if (getAddressPanel().getConsistAddress() == null) {
            return getThrottle();
        }
        return getConsistFunctionsPanel().getFunctionThrottle();        
    }    

    public RosterEntry getRosterEntry() {
        return addressPanel.getRosterEntry();
    }

    public RosterEntry getFunctionRosterEntry() {
        if (getAddressPanel().getConsistAddress() == null) {
            return getRosterEntry();
        }        
        return getConsistFunctionsPanel().getFunctionRosterEntry();                
    }    

    public ControlPanel getControlPanel() {
        if (controlPanel == null) { // init only when requested
            controlPanel = new ControlPanel(throttleManager, withPopupMenu);
            controlPanel.setAddressPanel(addressPanel);
        }        
        return controlPanel;
    }

    public FunctionPanel getFunctionPanel() {
        if (functionPanel == null) { // init only when requested
            functionPanel = new FunctionPanel(withPopupMenu);
            functionPanel.setAddressPanel(addressPanel);
        }          
        return functionPanel;
    }

    public SpeedPanel getSpeedPanel() {
        if (speedPanel == null) { // init only when requested
            speedPanel = new SpeedPanel();
            speedPanel.setAddressPanel(addressPanel);
        }
        return speedPanel;
    }

    public ConsistFunctionPanel getConsistFunctionsPanel() {
        if (consistFunctionsPanel == null) { // init only when requested
            consistFunctionsPanel = new ConsistFunctionPanel(throttleManager);
            consistFunctionsPanel.setAddressPanel(addressPanel);
        }
        return consistFunctionsPanel;
    }

    public BackgroundPanel getBackgroundPanel() {
        if (backgroundPanel == null) { // init only when requested
            backgroundPanel = new BackgroundPanel();
            backgroundPanel.setAddressPanel(addressPanel);
        }
        return backgroundPanel;
    }

    public LocoIconPanel getLocoIconPanel() {
        if (locoIconPanel == null) { // init only when requested
            locoIconPanel = new LocoIconPanel();
            locoIconPanel.setAddressPanel(addressPanel);
        }        
        return locoIconPanel;
    }

    public boolean hasActiveFunction() {
        if (getAddressPanel().getThrottle() == null) {
            return false;
        }
        for (boolean b : getAddressPanel().getThrottle().getFunctions() ) {
            if (b) {
                return true;
            }
        }
        return false;
    }   

    private void initGUI() {
        // create panels that are actually required for a throttle
        // some (speed Panel, backgroundPanel) will be created on demand only (see getters)
        addressPanel = new AddressPanel(throttleManager);            
        addressPanel.setEnabled(true);
        addressPanel.addAddressListener(this);                       
    }

    public void setRosterEntry(RosterEntry re) {
        getAddressPanel().setRosterEntry(re);
    }
    
    public void setAddress(DccLocoAddress la) {
        getAddressPanel().setCurrentAddress(la);
    }

    public DccLocoAddress getAddress() {
        return getAddressPanel().getCurrentAddress();
    }

    public void setConsistAddress(DccLocoAddress la) {
        getAddressPanel().setConsistAddress(la);
    }
        
    public void eStop() {
        DccThrottle throt = getAddressPanel().getThrottle();
        if (throt != null) {
            throt.setSpeedSetting(-1);
        }
    }

    /**
     * Handle my own destruction.
     * <ol>
     * <li> dispose of sub windows.
     * <li> notify my manager of my demise.
     * </ol>
     */
    public void dispose() {
        log.debug("Disposing");
        addressPanel.removeAddressListener(this);
        // should the throttle list table stop listening to that throttle?
        if (throttle!=null &&  throttleFrameManager.getNumberOfEntriesFor((DccLocoAddress) throttle.getLocoAddress()) == 0 ) { // 0 because this throtte frame window has been removed from the list already, so this is last chance to remove listener
            throttleManager.removeListener(throttle.getLocoAddress(), throttleFrameManager.getThrottlesListPanel().getTableModel());
            throttleFrameManager.getThrottlesListPanel().getTableModel().fireTableDataChanged();
        }
        // check for any special disposing in InternalFrames
        if (controlPanel!=null) {
            controlPanel.dispose();
        }
        if (functionPanel!=null) {
            functionPanel.dispose();
        }
        if (speedPanel!=null) {
            speedPanel.dispose();
        }
        if (backgroundPanel!=null) {
            backgroundPanel.dispose();      
        }
        if (consistFunctionsPanel!=null) {
            consistFunctionsPanel.dispose();
        }
        // dispose of this last because it will release and destroy the throttle.
        addressPanel.dispose();
    }

    public void saveRosterChanges() {
        RosterEntry rosterEntry = addressPanel.getRosterEntry();
        if (rosterEntry == null) {
            JmriJOptionPane.showMessageDialog(this.getAddressPanel(), Bundle.getMessage("ThrottleFrameNoRosterItemMessageDialog"),
                Bundle.getMessage("ThrottleFrameNoRosterItemTitleDialog"), JmriJOptionPane.ERROR_MESSAGE);
            return;
        }
        if ((!InstanceManager.getDefault(ThrottlesPreferences.class).isSavingThrottleOnLayoutSave()) && (JmriJOptionPane.showConfirmDialog(this.getAddressPanel(), Bundle.getMessage("ThrottleFrameRosterChangeMesageDialog"),
            Bundle.getMessage("ThrottleFrameRosterChangeTitleDialog"), JmriJOptionPane.YES_NO_OPTION) != JmriJOptionPane.YES_OPTION)) {
            return;
        }
        if (functionPanel!=null) {
            functionPanel.saveFunctionButtonsToRoster(rosterEntry);
        }
        if (controlPanel!=null) {
            controlPanel.saveToRoster(rosterEntry);
        }
    }

    /**
     * Collect the prefs of this object into the given XML Element array
     * 
     * @param children the array to fill with the XML Elements for this object.  The caller will add these to the parent element as needed.
     * 
     */
    public void getXml(ArrayList<Element> children) {
        if (controlPanel != null) {
            children.add(controlPanel.getXml());
        }
        if (functionPanel != null) {
            children.add(functionPanel.getXml());
        }
        children.add(addressPanel.getXml());
        if (speedPanel != null) { 
            children.add(speedPanel.getXml());
        }
        if (locoIconPanel != null) {
            children.add(locoIconPanel.getXml());
        }
        if (consistFunctionsPanel != null) {
            children.add(consistFunctionsPanel.getXml());
        }
    }

    /**
     * Set the preferences based on the XML Element.
     * <ul>
     * <li> Window prefs
     * <li> Frame title
     * <li> ControlPanel
     * <li> FunctionPanel
     * <li> AddressPanel
     * <li> SpeedPanel
     * </ul>
     *
     * @param e The Element for this object.
     */
    public void setXml(Element e) {
        if (e == null) {
            return;
        }
        Element child = e.getChild("AddressPanel");
        addressPanel.setXml(child);
        child = e.getChild("ControlPanel");
        if (child != null) {
            getControlPanel().setXml(child);
        }
        child = e.getChild("FunctionPanel");
        if (child != null) {
            getFunctionPanel().setXml(child);
        }
        child = e.getChild("SpeedPanel");
        if (child != null) {
            getSpeedPanel().setXml(child);
        }
        child = e.getChild("LocoIconPanel");
        if (child != null) {
            getLocoIconPanel().setXml(child);
        }
        child = e.getChild("ConsistFunctionsPanel");
        if (child != null &&
            //  SimpleThrottlePanel being used to implement ConsistFunctionsPanel, avoid loading recursively
            //  when using CS consist, xml for consist will be head unit one, that will be reloaded again and agin here
          (myThrottleController != null) && (myThrottleController.getThrottleControllersContainer() != null)) {
            getConsistFunctionsPanel().setXml(child);
        }
    }

    public void saveThrottleAs(Element throttleElement) {
        JFileChooser fileChooser = jmri.jmrit.XmlFile.userFileChooser(Bundle.getMessage("PromptXmlFileTypes"), "xml");
        fileChooser.setCurrentDirectory(new File(getDefaultThrottleFolder()));
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        java.io.File file = StoreXmlConfigAction.getFileName(fileChooser);
        if (file == null) {
            return;
        }
        saveThrottle(throttleElement, file.getAbsolutePath());
    }

    public void saveThrottle(Element throttleElement) {
        if (getRosterEntry() != null) {
            saveThrottle(throttleElement, ThrottleUICore.getDefaultThrottleFolder() + getRosterEntry().getId().trim() + ".xml");
        } else if (getLastUsedSaveFile() != null) {
            saveThrottle(throttleElement, getLastUsedSaveFile());
        }
    }

    private void saveThrottle(Element throttleElement, String sfile) {
        // Save throttle: title / window position
        // as strongly linked to extended throttles and roster presence, do not save function buttons and background window as they're stored in the roster entry
        XmlFile xf = new XmlFile() {
        };   // odd syntax is due to XmlFile being abstract
        xf.makeBackupFile(sfile);
        File file = new File(sfile);
        try {
            //The file does not exist, create it before writing
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                if (!parentDir.mkdir()) { // make directory and check result
                    log.error("could not make parent directory");
                }
            }
            if (!file.createNewFile()) { // create file, check success
                log.error("createNewFile failed");
            }
        } catch (IOException exp) {
            log.error("Exception while writing the throttle file, may not be complete: {}", exp.getMessage());
        }

        try {
            Element root = new Element("throttle-config");
            root.setAttribute("noNamespaceSchemaLocation",  // NOI18N
                    "http://jmri.org/xml/schema/throttle-config.xsd",  // NOI18N
                    org.jdom2.Namespace.getNamespace("xsi",
                            "http://www.w3.org/2001/XMLSchema-instance"));  // NOI18N
            Document doc = new Document(root);

            // add XSLT processing instruction
            // <?xml-stylesheet type="text/xsl" href="XSLT/throttle.xsl"?>
            java.util.Map<String,String> m = new java.util.HashMap<String, String>();
            m.put("type", "text/xsl");
            m.put("href", jmri.jmrit.XmlFile.xsltLocation + "throttle-config.xsl");
            org.jdom2.ProcessingInstruction p = new org.jdom2.ProcessingInstruction("xml-stylesheet", m);
            doc.addContent(0,p);
            
            // don't save the loco address or consist address
            //   throttleElement.getChild("AddressPanel").removeChild("locoaddress");
            //   throttleElement.getChild("AddressPanel").removeChild("locoaddress");
            if ((getRosterEntry() != null) &&
                    (ThrottleUICore.getDefaultThrottleFolder() + getRosterEntry().getId().trim() + ".xml").compareTo(sfile) == 0) // don't save function buttons labels, they're in roster entry
            {
                throttleElement.getChild("FunctionPanel").removeChildren("FunctionButton");
                saveRosterChanges();
            } 

            root.setContent(throttleElement);
            xf.writeXML(file, doc);
            setLastUsedSaveFile(sfile);
        } catch (IOException ex) {
            log.warn("Exception while storing throttle xml: {}", ex.getMessage());
        }
    }

    public Element loadThrottle(String sfile) throws IOException, NullPointerException, JDOMException, FileNotFoundException {
        log.debug("Loading throttle file : {}", sfile);
        if (sfile == null) {
            return null;
        }
        
        XmlFile xf = new XmlFile() {};   // odd syntax is due to XmlFile being abstract
        xf.setValidate(XmlFile.Validate.CheckDtdThenSchema);
        File f = new File(sfile);
        Element root = xf.rootFromFile(f);
        Element conf = root.getChild("ThrottleFrame");
        // File looks ok
        setLastUsedSaveFile(sfile);
        // and finally load all preferences
        setXml(conf);
        // and return it to be used by caller if needed
        return conf;
    }

    private boolean isLoadingDefault = false;

    public void loadDefaultThrottle() {
        if (isLoadingDefault) { // avoid looping on this method
            return; 
        }
        isLoadingDefault = true;
        String dtf = InstanceManager.getDefault(ThrottlesPreferences.class).getDefaultThrottleFilePath();
        if (dtf == null || dtf.isEmpty()) {
            return;
        }
        log.debug("Loading default throttle file : {}", dtf);
        if (myThrottleController!=null) {
            myThrottleController.loadThrottleFile(dtf);
        }
        setLastUsedSaveFile(null);
        isLoadingDefault = false;
    }

    @Override
    public void notifyAddressChosen(LocoAddress l) {
    }

    @Override
    public void notifyRosterEntrySelected(RosterEntry re) {     
    }

    @Override
    public void notifyAddressReleased(LocoAddress la) {
        if (throttle == null) {
            log.debug("notifyAddressReleased() throttle already null, called for loc {}",la);
            return;
        }        
        if (throttleFrameManager.getNumberOfEntriesFor((DccLocoAddress) throttle.getLocoAddress()) == 1 )  {
            throttleManager.removeListener(throttle.getLocoAddress(), throttleFrameManager.getThrottlesListPanel().getTableModel());
        }        throttle = null;
        setLastUsedSaveFile(null);
        if (myThrottleController!=null) {
            myThrottleController.updateFrameTitle();
            myThrottleController.updateGUI();
        }
        throttleFrameManager.getThrottlesListPanel().getTableModel().fireTableStructureChanged();        
    }

    @Override
    public void notifyAddressThrottleFound(DccThrottle t) {
        if (throttle != null) {
            log.debug("notifyAddressThrottleFound() throttle non null, called for loc {}",t.getLocoAddress());
            return;
        }
        throttle = t;
        if ((InstanceManager.getDefault(ThrottlesPreferences.class).isUsingExThrottle())
                && (InstanceManager.getDefault(ThrottlesPreferences.class).isAutoLoading()) && (addressPanel != null)) {
            if ((addressPanel.getRosterEntry() != null)
                    && ((getLastUsedSaveFile() == null) || (getLastUsedSaveFile().compareTo(getDefaultThrottleFolder() + addressPanel.getRosterEntry().getId().trim() + ".xml") != 0))) {
                if (myThrottleController!=null) {
                    myThrottleController.loadThrottleFile(getDefaultThrottleFolder() + addressPanel.getRosterEntry().getId().trim() + ".xml");
                }
                setLastUsedSaveFile(getDefaultThrottleFolder() + addressPanel.getRosterEntry().getId().trim() + ".xml");
            } else if ((addressPanel.getRosterEntry() == null)
                    && ((getLastUsedSaveFile() == null) || (getLastUsedSaveFile().compareTo(getDefaultThrottleFolder() + addressPanel.getCurrentAddress()+ ".xml") != 0))) {
                if (myThrottleController!=null) {
                    myThrottleController.loadThrottleFile(getDefaultThrottleFolder() + throttle.getLocoAddress().getNumber() + ".xml");
                }
                setLastUsedSaveFile(getDefaultThrottleFolder() + throttle.getLocoAddress().getNumber() + ".xml");
            }
        } else {
            if ((addressPanel != null) && (addressPanel.getRosterEntry() == null)) { // no known roster entry
                loadDefaultThrottle();
            }
        }
        if (myThrottleController!=null) {
            myThrottleController.updateFrameTitle();
            myThrottleController.updateGUI();
        }
        throttleFrameManager.getThrottlesListPanel().getTableModel().fireTableDataChanged();
    }
    
    @Override
    public void notifyConsistAddressChosen(LocoAddress l) {
        notifyAddressChosen(l);
    }
    
    @Override
    public void notifyConsistAddressReleased(LocoAddress la) {
        notifyAddressReleased(la);
    }

    @Override
    public void notifyConsistAddressThrottleFound(DccThrottle throttle) {
        notifyAddressThrottleFound(throttle);
    }

    public String getLastUsedSaveFile() {
        return lastUsedSaveFile;
    }

    public void setLastUsedSaveFile(String lusf) {
        lastUsedSaveFile = lusf;
        if (myThrottleController!=null) {
            myThrottleController.updateGUI();
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ThrottleUICore.class);
}
