package jmri.jmrit.throttle.UIImplementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import jmri.jmrit.throttle.panels.ControlPanel;
import jmri.jmrit.throttle.panels.FunctionPanel;
import jmri.jmrit.throttle.panels.SpeedPanel;
import jmri.jmrit.throttle.panels.LocoIconPanel;
import jmri.jmrit.throttle.preferences.ThrottlesPreferences;
import jmri.util.FileUtil;
import jmri.util.iharder.dnd.URIDrop;
import jmri.util.swing.JmriJOptionPane;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 * Should be named ThrottlePanel but was already existing with that name and
 * don't want to break dependencies (particularly in Jython code)
 *
 * @author Glen Oberhauser
 * @author Andrew Berridge Copyright 2010
 */
public class ThrottleUICore implements AddressListener, PropertyChangeListener  {

    private DccThrottle throttle;
    private final ThrottleManager throttleManager;
    private final ThrottleFrameManager throttleFrameManager = InstanceManager.getDefault(ThrottleFrameManager.class);
    private final ThrottleControllerUI myThrottleController;

    private ControlPanel controlPanel;
    private FunctionPanel functionPanel;
    private AddressPanel addressPanel;
    private BackgroundPanel backgroundPanel;
    private SpeedPanel speedPanel;
    private LocoIconPanel locoIconPanel;

    private String lastUsedSaveFile = null;

    private static final String DEFAULT_THROTTLE_FILENAME = "JMRI_ThrottlePreference.xml";

    public static String getDefaultThrottleFolder() {
        return FileUtil.getUserFilesPath() + "throttle" + File.separator;
    }

    public static String getDefaultThrottleFilename() {
        return getDefaultThrottleFolder() + DEFAULT_THROTTLE_FILENAME;
    }

    public ThrottleUICore(ThrottleManager tm, ThrottleControllerUI tc) {
        super();
        throttleManager = tm;
        myThrottleController = tc;
        InstanceManager.getDefault(ThrottlesPreferences.class).addPropertyChangeListener(this);
        initGUI();
        applyPreferences();
    }

    public ControlPanel getControlPanel() {
        return controlPanel;
    }

    public FunctionPanel getFunctionPanel() {
        return functionPanel;
    }

    public AddressPanel getAddressPanel() {
        return addressPanel;
    }

    public RosterEntry getRosterEntry() {
        return addressPanel.getRosterEntry();
    }

    public SpeedPanel getSpeedPanel() {
        return speedPanel;
    }

    public BackgroundPanel getBackgroundPanel() {
        return backgroundPanel;
    }

    public LocoIconPanel getLocoIconPanel() {
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

    /**
     * Place and initialize the GUI elements.
     * <ul>
     * <li> ControlPanel
     * <li> FunctionPanel
     * <li> AddressPanel
     * <li> SpeedPanel
     * <li> JMenu
     * </ul>
     */
    private void initGUI() {
        addressPanel = new AddressPanel(throttleManager);
        controlPanel = new ControlPanel(throttleManager);
        functionPanel = new FunctionPanel();
        speedPanel = new SpeedPanel();
        backgroundPanel = new BackgroundPanel();
        locoIconPanel = new LocoIconPanel();
            
        controlPanel.setEnabled(false);
        functionPanel.setEnabled(false);
        speedPanel.setEnabled(false);
        addressPanel.setEnabled(true);
        locoIconPanel.setEnabled(true);

        functionPanel.setAddressPanel(addressPanel); // so the function panel can get access to the roster
        controlPanel.setAddressPanel(addressPanel);
        backgroundPanel.setAddressPanel(addressPanel); // reusing same way to do it than existing thing in functionPanel
        speedPanel.setAddressPanel(addressPanel);
        locoIconPanel.setAddressPanel(addressPanel);

        addressPanel.addAddressListener(this);                       
    }

    private void applyPreferences() {
        loadDefaultThrottle();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ThrottlesPreferences.prefPopertyName.compareTo(evt.getPropertyName()) == 0) {
            applyPreferences();
        }        
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
        URIDrop.remove(backgroundPanel);
        addressPanel.removeAddressListener(this);
        // should the throttle list table stop listening to that throttle?
        if (throttle!=null &&  throttleFrameManager.getNumberOfEntriesFor((DccLocoAddress) throttle.getLocoAddress()) == 0 ) { // 0 because this throtte frame window has been removed from the list already, so this is last chance to remove listener
            throttleManager.removeListener(throttle.getLocoAddress(), throttleFrameManager.getThrottlesListPanel().getTableModel());
            throttleFrameManager.getThrottlesListPanel().getTableModel().fireTableDataChanged();
        }
        InstanceManager.getDefault(ThrottlesPreferences.class).removePropertyChangeListener(this);
        // check for any special disposing in InternalFrames
        controlPanel.dispose();
        functionPanel.dispose();
        speedPanel.dispose();
        backgroundPanel.dispose();      
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
        functionPanel.saveFunctionButtonsToRoster(rosterEntry);
        controlPanel.saveToRoster(rosterEntry);
    }

    /**
     * Collect the prefs of this object into the given XML Element array
     * 
     * @param children the array to fill with the XML Elements for this object.  The caller will add these to the parent element as needed.
     * 
     */
    public void getXml(ArrayList<Element> children) {
        children.add(controlPanel.getXml());
        children.add(functionPanel.getXml());
        children.add(addressPanel.getXml());
        children.add(speedPanel.getXml());
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
        Element controlPanelElement = e.getChild("ControlPanel");
        controlPanel.setXml(controlPanelElement);
        Element functionPanelElement = e.getChild("FunctionPanel");
        functionPanel.setXml(functionPanelElement);
        Element addressPanelElement = e.getChild("AddressPanel");
        addressPanel.setXml(addressPanelElement);
        Element speedPanelElement = e.getChild("SpeedPanel");
        if (speedPanelElement != null) { // older throttle configs may not have this element
            speedPanel.setXml(speedPanelElement);
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
        myThrottleController.loadThrottleFile(dtf);
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
        }        
        throttle = null;
        setLastUsedSaveFile(null);        
        myThrottleController.updateFrameTitle();
        myThrottleController.updateGUI(); 
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
                myThrottleController.loadThrottleFile(getDefaultThrottleFolder() + addressPanel.getRosterEntry().getId().trim() + ".xml");
                setLastUsedSaveFile(getDefaultThrottleFolder() + addressPanel.getRosterEntry().getId().trim() + ".xml");
            } else if ((addressPanel.getRosterEntry() == null)
                    && ((getLastUsedSaveFile() == null) || (getLastUsedSaveFile().compareTo(getDefaultThrottleFolder() + addressPanel.getCurrentAddress()+ ".xml") != 0))) {
                myThrottleController.loadThrottleFile(getDefaultThrottleFolder() + throttle.getLocoAddress().getNumber() + ".xml");
                setLastUsedSaveFile(getDefaultThrottleFolder() + throttle.getLocoAddress().getNumber() + ".xml");
            }
        } else {
            if ((addressPanel != null) && (addressPanel.getRosterEntry() == null)) { // no known roster entry
                loadDefaultThrottle();
            }
        }
        myThrottleController.updateFrameTitle();
        myThrottleController.updateGUI();        
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
        myThrottleController.updateGUI();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ThrottleUICore.class);
}
