package jmri.jmrit.throttle;

import java.util.ArrayList;
import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.swing.JFrame;

import org.jdom2.Element;

import jmri.DccLocoAddress;
import jmri.InstanceManagerAutoDefault;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.throttle.interfaces.ThrottleControllerUI;
import jmri.jmrit.throttle.interfaces.ThrottleControllersUIContainer;
import jmri.jmrit.throttle.list.ThrottlesListPanel;
import jmri.jmrit.throttle.preferences.ThrottlesPreferencesWindow;
import jmri.util.JmriJFrame;

/**
 * Interface for allocating and deallocating throttles frames. Not to be
 * confused with ThrottleManager.
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
 * @author Lionel Jeanson
 * 
 */
public class ThrottleFrameManager implements InstanceManagerAutoDefault {

    private int activeFrame;
    private int frameCounterID = 0; // to generate unique names for each card    

    private ArrayList<ThrottleControllersUIContainer> throttleUIContainers; // synchronized access

    private ThrottlesPreferencesWindow throttlePreferencesFrame;
    private JmriJFrame throttlesListFrame;
    private ThrottlesListPanel throttlesListPanel;

    /**
     * Constructor for the ThrottleFrameManager object.
     */
    public ThrottleFrameManager() {
        throttleUIContainers = new ArrayList<>(0);
    }

    /**
     * Ask this manager to create a new Throttle Window
     *
     * @return The newly created ThrottleWindow
     */
    public ThrottleWindow createThrottleWindow() {
        return createThrottleWindow((jmri.jmrix.ConnectionConfig) null);
    }

    /**
     * Ask this manager to create a new Throttle Window
     *
     * @param connectionConfig the connection config
     * @return The newly created ThrottleWindow
     */
    public ThrottleWindow createThrottleWindow(jmri.jmrix.ConnectionConfig connectionConfig) {
        ThrottleWindow tw = new ThrottleWindow(connectionConfig);
        tw.pack();
        synchronized (this) {
            throttleUIContainers.add(tw);
            activeFrame = throttleUIContainers.indexOf(tw);
        }
        getThrottlesListPanel().getTableModel().fireTableStructureChanged();
        return tw;
    }

    /**
     * Ask this manager to create a new Throttle Window
     *
     * @param e the xml element for the throttle window
     * @return The newly created ThrottleWindow
     */
    public ThrottleWindow createThrottleWindow(Element e) {
        ThrottleWindow tw = ThrottleWindow.createThrottleWindow(e);
        tw.pack();
        synchronized (this) {
            throttleUIContainers.add(tw);
            activeFrame = throttleUIContainers.indexOf(tw);
        }
        getThrottlesListPanel().getTableModel().fireTableStructureChanged();
        return tw;
    }

    /**
     * Ask this manager to create a new Throttle Frame
     * A ThrottleWindow will be created, but the inner Panel (ThrottleFrame)
     * will be returned.
     * 
     * This method is backward compatible with the first implementation of JMRI throttle (200x).
     *
     * @return The newly created ThrottleFrame
     */
    public ThrottleControllerUI createThrottleFrame() {
        return createThrottleFrame(null);
    }    

    /**
     * Ask this manager to create a new Throttle Frame
     * A ThrottleWindow will be created, but the inner Panel (ThrottleFrame)
     * will be returned.
     * 
     * This method is backward compatible with the first implementation of JMRI throttle (200x).
     *
     * @param connectionConfig the connection config
     * @return The newly created ThrottleFrame
     */
    public ThrottleControllerUI createThrottleFrame(jmri.jmrix.ConnectionConfig connectionConfig) {
        return createThrottleWindow(connectionConfig).getCurentThrottleController();
    }

    /**
     * Ask this manager to create a new Simple Throttle Frame
     * A SimpleThrottleWindow will be created, but the inner Panel (SimpleThrottleFrame)
     * will be returned.
     * 
     * @param re the RosterEntry that this throttle should control
     *
     * @return The newly created SimpleThrottleFrame
     */
    public ThrottleControllerUI createSimpleThrottleFrame(RosterEntry re) {
        return createSimpleThrottleFrame(null,re.getDccLocoAddress());
    }

    /**
     * Ask this manager to create a new Simple Throttle Frame
     * A SimpleThrottleWindow will be created, but the inner Panel (SimpleThrottleFrame)
     * will be returned.
     * 
     * @param la the loco address that this throttle should control
     *
     * @return The newly created SimpleThrottleFrame
     */
    public ThrottleControllerUI createSimpleThrottleFrame(DccLocoAddress la) {
        return createSimpleThrottleFrame(null, la);
    } 

    /**
     * Ask this manager to create a new Simple Throttle Frame
     * A SimpleThrottleWindow will be created, but the inner Panel (SimpleThrottleFrame)
     * will be returned.
     * 
     * @param connectionConfig the connection config
     * @param la the loco address that this throttle should control
     *
     * @return The newly created SimpleThrottleFrame
     */    
    public ThrottleControllerUI createSimpleThrottleFrame(jmri.jmrix.ConnectionConfig connectionConfig, DccLocoAddress la) {
        SimpleThrottleWindow stw = new SimpleThrottleWindow(connectionConfig, la);
        stw.pack();
        stw.setVisible(true);
        synchronized (this) {
            throttleUIContainers.add(stw);
            activeFrame = throttleUIContainers.indexOf(stw);
        }
        getThrottlesListPanel().getTableModel().fireTableStructureChanged();
        return stw.getThrottleControllerAt(0);
    }

    /**
     * Request that this manager destroy a ThrottleControllersUIContainer. 
     * Is called by the ThrottleWindow, or SimpleThrottleWindow, when it is disposed
     *
     * @param throtCont The to-be-destroyed Throttle Container
     */
    public void requestThrottleWindowDestruction(ThrottleControllersUIContainer throtCont) {
        if (throtCont != null) {
            destroyThrottleWindow(throtCont);
            synchronized (this) {
                throttleUIContainers.remove(throtCont);
                if (!throttleUIContainers.isEmpty()) {
                    requestFocusForNextThrottleWindow();
                }
            }
        }
        getThrottlesListPanel().getTableModel().fireTableStructureChanged();        
    }

    /**
     * Request that this manager destroy all throttle containers.
     */    
    public synchronized void requestAllThrottleWindowsDestroyed() {
        for (ThrottleControllersUIContainer frame : throttleUIContainers) {
            destroyThrottleWindow(frame);
        }
        throttleUIContainers = new ArrayList<>(0);
        getThrottlesListPanel().getTableModel().fireTableStructureChanged();        
    }

    /**
     * Request this manager for a unique identifier (used by ThrottleWindow to identify themselves).
     * 
     * @return a unique identifier
     * 
     */         
    public int generateUniqueFrameID() {
         return frameCounterID++;
    }

    /**
     * Perform the destruction of a Throttle UI containers 
     *
     * @param throtCont The ThrottleFrame to be destroyed.
     */
    private void destroyThrottleWindow(ThrottleControllersUIContainer throtCont) {
        throttleUIContainers.remove(throtCont);        
        getThrottlesListPanel().getTableModel().fireTableStructureChanged();        
    }
    
    /**
     * Gets an iterator over all the Throttle UI containers
     * 
     * @return an iterator over all the Throttle UI containers
     */    
    public Iterator<ThrottleControllersUIContainer> iterator() {
        return throttleUIContainers.iterator();
    }
       
    /**
     * Return the number of active thottle UI containers
     *
     * @return the number of active thottle UI containers
     */
    public synchronized int getNbThrottleControllersContainers() {
        return throttleUIContainers.size();
    }
    
    /**
     * Return the thottle controller container at nth position in the list
     *
     * @param n position of the throttle controller container
     * @return a thottle controller container
     */ 
    public synchronized ThrottleControllersUIContainer getThrottleControllersContainerAt(int n) {
        if (! (n < throttleUIContainers.size())) {
            return null;
        }
        return throttleUIContainers.get(n);
    }

    public synchronized void requestFocusForNextThrottleWindow() {
        activeFrame = (activeFrame + 1) % throttleUIContainers.size();
        JmriJFrame tw = (JmriJFrame) throttleUIContainers.get(activeFrame);
        tw.requestFocus();
        tw.toFront();
    }

    public synchronized void requestFocusForPreviousThrottleWindow() {
        activeFrame--;
        if (activeFrame < 0) {
            activeFrame = throttleUIContainers.size() - 1;
        }
        JmriJFrame tw =(JmriJFrame) throttleUIContainers.get(activeFrame);
        tw.requestFocus();
        tw.toFront();
    }

    public synchronized JmriJFrame getCurentThrottleController() {
        if (throttleUIContainers == null) {
            return null;
        }
        if (throttleUIContainers.isEmpty()) {
            return null;
        }
        return  (JmriJFrame) throttleUIContainers.get(activeFrame);
    }

    public ThrottlesListPanel getThrottlesListPanel() {
        if (throttlesListPanel == null) {
            throttlesListPanel = new ThrottlesListPanel();
        }
        return throttlesListPanel;
    }

    /*
     * Show JMRI native throttle list window
     *
     */
    public void showThrottlesList() {
        if (throttlesListFrame == null) {            
            throttlesListFrame = new JmriJFrame(Bundle.getMessage("ThrottleListFrameTile"));        
            throttlesListFrame.setContentPane(getThrottlesListPanel());
            throttlesListFrame.pack();            
        }
        throttlesListFrame.setVisible(true);
    }

    /*
     * Show throttle preferences window
     *
     */
    public void showThrottlesPreferences() {
        if (throttlePreferencesFrame == null) {
            throttlePreferencesFrame = new ThrottlesPreferencesWindow(Bundle.getMessage("ThrottlePreferencesFrameTitle"));
            throttlePreferencesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            throttlePreferencesFrame.pack();
        } else {
            throttlePreferencesFrame.resetComponents();
            throttlePreferencesFrame.revalidate();
        }
        throttlePreferencesFrame.setVisible(true);
        throttlePreferencesFrame.requestFocus();
    }

    /**
     * Force emergency stop of all managed throttles windows
     *
     */   
    public void emergencyStopAll() {
        throttleUIContainers.forEach(tw -> {
            tw.emergencyStopAll();
        });
    }
    
    /**
     * Return the number of throttle controllers for a LocoAddress,
     * usefull to know if a layout throttle object should actually be released
     *
     * @param la locoaddrress we're looking for
     * @return the number of throttle controllers for that LocoAddress
     */   
    public int getNumberOfEntriesFor(@CheckForNull DccLocoAddress la) {
        if (la == null) { 
            return 0; 
        }
        int ret = 0;
        for (ThrottleControllersUIContainer tw : throttleUIContainers) {        
            ret += tw.getNumberOfEntriesFor(la);
        }
        return ret;
    }

    // private static final Logger log = LoggerFactory.getLogger(ThrottleFrameManager.class);
}
