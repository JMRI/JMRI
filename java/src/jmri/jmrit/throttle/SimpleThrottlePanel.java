package jmri.jmrit.throttle;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdom2.Element;
import org.jdom2.JDOMException;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.ThrottleManager;
import jmri.configurexml.LoadXmlConfigAction;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.swing.RosterEntrySelectorPanel;
import jmri.jmrit.throttle.UIImplementation.ThrottleUICore;
import jmri.jmrit.throttle.interfaces.AddressListener;
import jmri.jmrit.throttle.interfaces.ThrottleControllerUI;
import jmri.jmrit.throttle.interfaces.ThrottleControllersUIContainer;
import jmri.jmrit.throttle.panels.ControlPanel;
import jmri.jmrit.throttle.preferences.ThrottlesPreferences;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The classic JMRI throttle implementation as a JDesktopPane
 * 
 * Class naming is bad but kept for backwards compatibility. This is the main class for the throttle UI, it contains the 4 main panels (address, control, function and speed) as JInternalFrames and manages them. It also manages the Jynstruments that can be added to the throttle frame.
 *
 * @author Lionel Jeanson Copyright 2026
 *
 */
public class SimpleThrottlePanel extends JPanel implements ThrottleControllerUI {

    private ThrottleControllersUIContainer myContainer;
    private final ThrottleManager throttleManager;
    private final ThrottleFrameManager throttleFrameManager = InstanceManager.getDefault(ThrottleFrameManager.class);
    private final ThrottleUICore throuic;
    private boolean isLoadingDefault = false;

    public SimpleThrottlePanel(SimpleThrottleWindow stw, ThrottleManager tm) {
        super();
        myContainer = stw;
        throttleManager = tm;
        throuic = new ThrottleUICore(throttleManager, this);
        initGUI();
        throttleFrameManager.getThrottlesListPanel().getTableModel().fireTableStructureChanged();
    }

    private void initGUI() {
        setLayout(new BorderLayout());        
        add(throuic.getControlPanel(), BorderLayout.WEST);
        add(throuic.getFunctionPanel(), BorderLayout.CENTER);
        add(throuic.getLocoIconPanel(), BorderLayout.NORTH);        
    }

    @Override
    public void updateGUI() {
        // nothing to do in this simple implementation as the GUI is directly updated by the ThrottleUICore
    }

    @Override
    public void updateFrameTitle() {
        String winTitle = Bundle.getMessage("ThrottleTitle");
        if ((throuic.getRosterEntry() != null) && (throuic.getRosterEntry().getId() != null)&& (throuic.getRosterEntry().getId().length() > 0)) {
            winTitle = winTitle + " - " + throuic.getRosterEntry().getId();            
        }
        if ( getThrottle() != null) {
            String addr  = throuic.getAddressPanel().getCurrentAddress().toString();
            winTitle = winTitle + " - " + addr;

        }
        if (myContainer != null && myContainer instanceof Frame) {
            ((Frame) myContainer).setTitle(winTitle);
        } else {
            log.warn("Unable to set simple throttle window title, myContainer is not an instance of Frame");
        }
    }

    public void resetFunctionPanelButton() {
        throuic.getFunctionPanel().resetFnButtons();
        throuic.getFunctionPanel().setEnabled();
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
        throuic.dispose();        
    }

    @Override
    public ThrottleControllersUIContainer getThrottleControllersContainer() {
        return myContainer;
    }
    
    @Override
    public void setThrottleControllersContainer(ThrottleControllersUIContainer tw) {
        myContainer = tw;
    }

    @Override
    public void toFront() {
        if (myContainer == null) {
            return;
        }
        if (myContainer instanceof Window) {
            ((Window) myContainer).toFront();
        } else {
            log.warn("Unable to set simple throttle window to front, myContainer is not an instance of Window");
        }
    }

    @Override
    public void setRosterEntry(RosterEntry re) {
        throuic.setRosterEntry(re);
    }

    @Override
    public RosterEntry getRosterEntry() {
        return throuic.getRosterEntry();
    }    

    @Override
    public void setAddress(DccLocoAddress la) {
        throuic.setAddress(la);
    }

    @Override
    public DccLocoAddress getAddress() {
        return throuic.getAddress();
    }

    @Override
    public void setConsistAddress(DccLocoAddress la) {
        throuic.setConsistAddress(la);
    }

     @Override
    public boolean isUsingAddress(DccLocoAddress la) {                    
        if ( getThrottle() != null && 
                ( la.equals( throuic.getAddressPanel().getCurrentAddress()) || la.equals( throuic.getAddressPanel().getConsistAddress()) ) ) {
            return true;
        }
        return false;
    }

    @Override
    public void eStop() {
        throuic.eStop();
    }

    @Override
    public boolean isRunning() {
        return ((getThrottle() != null) && (getThrottle().getSpeedSetting() > 0));
    }

    @Override
    public boolean isActive() {
        return ( (getThrottle() != null) && ( (getThrottle().getSpeedSetting() > 0) || (throuic.hasActiveFunction())));
    }

    @Override
    public DccThrottle getThrottle() {
        return throuic.getAddressPanel().getThrottle();  
    }

    @Override
    public DccThrottle getFunctionThrottle() {
        return throuic.getAddressPanel().getFunctionThrottle();  
     }

    @Override
    public JLabel getLabel() {
        JLabel ret = new JLabel(throuic.getLocoIconPanel().getLabel().getText(), throuic.getLocoIconPanel().getLabel().getIcon(), JLabel.CENTER);
        return ret;
    }

    @Override
    public boolean isSpeedDisplayContinuous() {
        return throuic.getControlPanel().getDisplaySlider() == ControlPanel.SLIDERDISPLAYCONTINUOUS;
    }

    public void saveRosterChanges() {
        throuic.saveRosterChanges();
    }

    @Override
    public RosterEntrySelectorPanel getRosterEntrySelector() {
        return throuic.getAddressPanel().getRosterEntrySelector();
    }

    @Override
    public void addAddressListener(AddressListener l) {
        throuic.getAddressPanel().addAddressListener(l);
    }

    @Override
    public void removeAddressListener(AddressListener l) {
        throuic.getAddressPanel().removeAddressListener(l);
    }

    @Override
    public void dispatchAddress() {
        throuic.getAddressPanel().dispatchAddress();
    }

    /**
     * Sets the location of a throttle frame on the screen according to x and y
     * coordinates
     *
     * @see java.awt.Component#setLocation(int, int)
     */
    @Override
    public void setLocation(int x, int y) {
        if (myContainer instanceof SimpleThrottleWindow) {
            ((SimpleThrottleWindow) myContainer).setLocation(x, y);
        } else {
            log.warn("Unable to set simple throttle window location, myContainer is not an instance of SimpleThrottleWindow");
        }
    }

    public Element getXmlFile() {
        if (throuic.getLastUsedSaveFile() == null) { // || (getRosterEntry()==null))
            return null;
        }
        Element me = new Element("ThrottleFrame");
        me.setAttribute("ThrottleXMLFile", FileUtil.getPortableFilename(throuic.getLastUsedSaveFile()));
        return me;
    }

    public void setXml(Element e) {
        if (e == null) {
            return;
        }

        String sfile = e.getAttributeValue("ThrottleXMLFile");
        if (sfile != null) {
            loadThrottleFile(FileUtil.getExternalFilename(sfile));
            return;
        }
    
    }

    @Override
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
        loadThrottleFile(dtf);
        throuic.setLastUsedSaveFile(null);
        isLoadingDefault = false;
    }

    public void loadThrottle() {
        JFileChooser fileChooser = jmri.jmrit.XmlFile.userFileChooser(Bundle.getMessage("PromptXmlFileTypes"), "xml");
        fileChooser.setCurrentDirectory(new File(ThrottleUICore.getDefaultThrottleFolder()));
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        java.io.File file = LoadXmlConfigAction.getFile(fileChooser, this);
        if (file == null) {
            return ;
        }
        loadThrottleFile(file.getAbsolutePath());
    }

    @Override
    public void loadThrottleFile(String sfile) {
        if (sfile == null) {
            loadThrottle();
            return;
        }

        try {
            Element conf = throuic.loadThrottle(sfile);
            setXml(conf);
        } catch (FileNotFoundException ex) {
            // Don't show error dialog if file is not found
            log.debug("Loading throttle exception: {}", ex.getMessage());
            log.debug("Tried loading throttle file \"{}\" , reverting to default, if any", sfile);
            loadDefaultThrottle(); // revert to loading default one
        } catch (NullPointerException | IOException | JDOMException ex) {
            log.debug("Loading throttle exception: {}", ex.getMessage());
            log.debug("Tried loading throttle file \"{}\" , reverting to default, if any", sfile);
            jmri.configurexml.ConfigXmlManager.creationErrorEncountered(
                    null, "parsing file " + sfile,
                    "Parse error", null, null, ex);
            loadDefaultThrottle(); // revert to loading default one
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SimpleThrottlePanel.class);    
}
