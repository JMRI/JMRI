package jmri.jmrit.throttle.implementation;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import jmri.jmrit.throttle.ThrottleFrameManager;
import jmri.jmrit.throttle.interfaces.AddressListener;
import jmri.jmrit.throttle.interfaces.ThrottleControllerUI;
import jmri.jmrit.throttle.interfaces.ThrottleControllersUIContainer;
import jmri.jmrit.throttle.panels.ControlPanel;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A basic throttle panel, with control and function panels only
 * No addressPanel, setAddress() or setConsistAddress() or setRosterEntry() has to be called
 * to initiate a throttle request
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
 * @author Lionel Jeanson Copyright 2026
 *
 */
public class SimpleThrottlePanel extends JPanel implements ThrottleControllerUI {

    private ThrottleControllersUIContainer myContainer;
    private final ThrottleManager throttleManager;
    private final ThrottleFrameManager throttleFrameManager = InstanceManager.getDefault(ThrottleFrameManager.class);
    protected final ThrottleUICore throuic;

    public SimpleThrottlePanel(ThrottleControllersUIContainer stw, ThrottleManager tm, boolean isShowingCtrlPanel, boolean isShowingFnPanel, boolean isShowingIconPanel) {
        super();
        myContainer = stw;
        throttleManager = tm;
        throuic = new ThrottleUICore(throttleManager, this, false);
        initGUI(isShowingCtrlPanel, isShowingFnPanel, isShowingIconPanel);
        throuic.loadDefaultThrottle();
        throttleFrameManager.getThrottlesListPanel().getTableModel().fireTableStructureChanged();
    }

    private void initGUI(boolean isShowingCtrlPanel, boolean isShowingFnPanel, boolean isShowingIconPanel) {    
        setLayout(new BorderLayout());
        setOpaque(false);
        if (isShowingCtrlPanel) {
            add(throuic.getControlPanel(), BorderLayout.WEST);
        }
        if (isShowingFnPanel) {
            add(throuic.getFunctionPanel(), BorderLayout.CENTER);
        }
        if (isShowingIconPanel) {
            add(throuic.getLocoIconPanel(), BorderLayout.NORTH);
        }
        setPreferredSize(new Dimension(450,350));        
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
            log.debug("Unable to set simple throttle window title, myContainer is not an instance of Frame");
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
        throuic.dispose();
        removeAll();
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
        return throuic.getThrottle();  
    }

    @Override
    public DccThrottle getFunctionThrottle() {
        return throuic.getFunctionThrottle();        
    }

    @Override
    public RosterEntry getFunctionRosterEntry() {
        return throuic.getFunctionRosterEntry();                
    }

    @Override
    public JLabel getLabel() {
        return new JLabel(throuic.getLocoIconPanel().getDescription(), throuic.getLocoIconPanel().getIcon(), JLabel.CENTER);
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
        if (myContainer instanceof Window) {
            ((Window) myContainer).setLocation(x, y);
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
    public void loadThrottleFile(String sfile) {
        if (sfile == null) {
            JFileChooser fileChooser = jmri.jmrit.XmlFile.userFileChooser(Bundle.getMessage("PromptXmlFileTypes"), "xml");
            fileChooser.setCurrentDirectory(new File(ThrottleUICore.getDefaultThrottleFolder()));
            fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            java.io.File file = LoadXmlConfigAction.getFile(fileChooser, this);
            if (file == null) {
                return ;
            }
        }

        try {
            Element conf = throuic.loadThrottle(sfile);
            setXml(conf);
        } catch (FileNotFoundException ex) {
            // Don't show error dialog if file is not found
            log.debug("Loading throttle exception: {}", ex.getMessage());
            log.debug("Tried loading throttle file \"{}\" , reverting to default, if any", sfile);
            throuic.loadDefaultThrottle(); // revert to loading default one
        } catch (NullPointerException | IOException | JDOMException ex) {
            log.debug("Loading throttle exception: {}", ex.getMessage());
            log.debug("Tried loading throttle file \"{}\" , reverting to default, if any", sfile);
            jmri.configurexml.ConfigXmlManager.creationErrorEncountered(
                    null, "parsing file " + sfile,
                    "Parse error", null, null, ex);
            throuic.loadDefaultThrottle(); // revert to loading default one
        }
    }

    private static final Logger log = LoggerFactory.getLogger(SimpleThrottlePanel.class);    
}
