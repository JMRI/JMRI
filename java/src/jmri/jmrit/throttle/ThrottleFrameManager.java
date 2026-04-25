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
 * @author Glen Oberhauser
 */
public class ThrottleFrameManager implements InstanceManagerAutoDefault {

    private int activeFrame;
    private int frameCounterID = 0; // to generate unique names for each card    

    private ArrayList<ThrottleControllersUIContainer> throttleWindows; // synchronized access

    private ThrottlesPreferencesWindow throttlePreferencesFrame;
    private JmriJFrame throttlesListFrame;
    private ThrottlesListPanel throttlesListPanel;

    /**
     * Constructor for the ThrottleFrameManager object.
     */
    public ThrottleFrameManager() {
        throttleWindows = new ArrayList<>(0);
    }

    /**
     * Tell this manager that a new ThrottleWindow was created.
     *
     * @return The newly created ThrottleWindow
     */
    public ThrottleWindow createThrottleWindow() {
        return createThrottleWindow((jmri.jmrix.ConnectionConfig) null);
    }

    /**
     * Tell this manager that a new ThrottleWindow was created.
     *
     * @param connectionConfig the connection config
     * @return The newly created ThrottleWindow
     */
    public ThrottleWindow createThrottleWindow(jmri.jmrix.ConnectionConfig connectionConfig) {
        ThrottleWindow tw = new ThrottleWindow(connectionConfig);
        tw.pack();
        synchronized (this) {
            throttleWindows.add(tw);
            activeFrame = throttleWindows.indexOf(tw);
        }
        getThrottlesListPanel().getTableModel().fireTableStructureChanged();
        return tw;
    }

    /**
     * Tell this manager that a new ThrottleWindow was created.
     *
     * @param e the xml element for the throttle window
     * @return The newly created ThrottleWindow
     */
    public ThrottleWindow createThrottleWindow(Element e) {
        ThrottleWindow tw = ThrottleWindow.createThrottleWindow(e);
        tw.pack();
        synchronized (this) {
            throttleWindows.add(tw);
            activeFrame = throttleWindows.indexOf(tw);
        }
        getThrottlesListPanel().getTableModel().fireTableStructureChanged();
        return tw;
    }

    /**
     * Tell this manager that a new ThrottleFrame was created.
     *
     * @return The newly created ThrottleFrame
     */
    public ThrottleControllerUI createThrottleFrame() {
        return createThrottleFrame(null);
    }    

    /**
     * Tell this manager that a new ThrottleFrame was created.
     *
     * @param connectionConfig the connection config
     * @return The newly created ThrottleFrame
     */
    public ThrottleControllerUI createThrottleFrame(jmri.jmrix.ConnectionConfig connectionConfig) {
        return createThrottleWindow(connectionConfig).getCurentThrottleController();
    }

    public ThrottleControllerUI createSimpleThrottleFrame(RosterEntry re) {
        return createSimpleThrottleFrame(null,re.getDccLocoAddress());
    }

    public ThrottleControllerUI createSimpleThrottleFrame(DccLocoAddress la) {
        return createSimpleThrottleFrame(null, la);
    } 

    public ThrottleControllerUI createSimpleThrottleFrame(jmri.jmrix.ConnectionConfig connectionConfig, DccLocoAddress la) {
        SimpleThrottleWindow stw = new SimpleThrottleWindow(connectionConfig, la);
        stw.pack();
        stw.setVisible(true);
        synchronized (this) {
            throttleWindows.add(stw);
            activeFrame = throttleWindows.indexOf(stw);
        }
        getThrottlesListPanel().getTableModel().fireTableStructureChanged();
        return stw.getThrottleControllerAt(0);
    }

    /**
     * Request that this manager destroy a throttle frame. Is called by the Throttle window when it is disposed
     *
     * @param frame The to-be-destroyed ThrottleFrame
     */
    public void requestThrottleWindowDestruction(ThrottleControllersUIContainer frame) {
        if (frame != null) {
            destroyThrottleWindow(frame);
            synchronized (this) {
                throttleWindows.remove(frame);
                if (!throttleWindows.isEmpty()) {
                    requestFocusForNextThrottleWindow();
                }
            }
        }
        getThrottlesListPanel().getTableModel().fireTableStructureChanged();        
    }

    public synchronized void requestAllThrottleWindowsDestroyed() {
        for (ThrottleControllersUIContainer frame : throttleWindows) {
            destroyThrottleWindow(frame);
        }
        throttleWindows = new ArrayList<>(0);
        getThrottlesListPanel().getTableModel().fireTableStructureChanged();        
    }
    
    public int generateUniqueFrameID() {
         return frameCounterID++;
    }

    /**
     * Perform the destruction of a ThrottleFrame. This method will not affect
     * the throttleFrames list, thus ensuring no synchronozation problems.
     *
     * @param window The ThrottleFrame to be destroyed.
     */
    private void destroyThrottleWindow(ThrottleControllersUIContainer window) {
        throttleWindows.remove(window);        
        getThrottlesListPanel().getTableModel().fireTableStructureChanged();        
    }
    
    public Iterator<ThrottleControllersUIContainer> iterator() {
        return throttleWindows.iterator();
    }
       
    /**
     * Return the number of active thottle windows
     *
     * @return the number of active thottle windows
     */
    public synchronized int getNbThrottleControllersContainers() {
        return throttleWindows.size();
    }
    
    /**
     * Return the thottle controller container at nth position in the list
     *
     * @param n position of the throttle controller container
     * @return a thottle controller container
     */ 
    public synchronized ThrottleControllersUIContainer getThrottleControllersContainerAt(int n) {
        if (! (n < throttleWindows.size())) {
            return null;
        }
        return throttleWindows.get(n);
    }

    public synchronized void requestFocusForNextThrottleWindow() {
        activeFrame = (activeFrame + 1) % throttleWindows.size();
        JmriJFrame tw = (JmriJFrame) throttleWindows.get(activeFrame);
        tw.requestFocus();
        tw.toFront();
    }

    public synchronized void requestFocusForPreviousThrottleWindow() {
        activeFrame--;
        if (activeFrame < 0) {
            activeFrame = throttleWindows.size() - 1;
        }
        JmriJFrame tw =(JmriJFrame) throttleWindows.get(activeFrame);
        tw.requestFocus();
        tw.toFront();
    }

    public synchronized JmriJFrame getCurentThrottleController() {
        if (throttleWindows == null) {
            return null;
        }
        if (throttleWindows.isEmpty()) {
            return null;
        }
        return  (JmriJFrame) throttleWindows.get(activeFrame);
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

    /*
     * Apply curent throttle preferences to all throttle windows
     *
     */
/*    public void applyPreferences() {
        throttleWindows.forEach(tw -> {
            tw.applyPreferences();
        });
        getThrottlesListPanel().applyPreferences();
    }*/


    /**
     * Force emergency stop of all managed throttles windows
     *
     */   
    public void emergencyStopAll() {
        throttleWindows.forEach(tw -> {
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
        for (ThrottleControllersUIContainer tw : throttleWindows) {        
            ret += tw.getNumberOfEntriesFor(la);
        }
        return ret;
    }

    // private static final Logger log = LoggerFactory.getLogger(ThrottleFrameManager.class);
}
