package jmri.jmrit.throttle;

import java.util.*;

import javax.annotation.CheckForNull;
import javax.swing.JFrame;

import jmri.DccLocoAddress;
import jmri.InstanceManagerAutoDefault;
import jmri.util.JmriJFrame;

import org.jdom2.Element;

/**
 * Interface for allocating and deallocating throttles frames. Not to be
 * confused with ThrottleManager.
 *
 * @author Glen Oberhauser
 */
public class ThrottleFrameManager implements InstanceManagerAutoDefault, ThrottleControllersUIContainersManager {

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
        throttlesListPanel.getTableModel().fireTableStructureChanged();
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
        throttlesListPanel.getTableModel().fireTableStructureChanged();
        return tw;
    }

    /**
     * Tell this manager that a new ThrottleFrame was created.
     *
     * @return The newly created ThrottleFrame
     */
    public ThrottleFrame createThrottleFrame() {
        return createThrottleFrame(null);
    }
    
    @Override
    public ThrottleControllerUI createThrottleController() {
        return createThrottleFrame();
    }

    /**
     * Tell this manager that a new ThrottleFrame was created.
     *
     * @param connectionConfig the connection config
     * @return The newly created ThrottleFrame
     */
    public ThrottleFrame createThrottleFrame(jmri.jmrix.ConnectionConfig connectionConfig) {
        return createThrottleWindow(connectionConfig).getCurrentThrottleFrame();
    }

    /**
     * Request that this manager destroy a throttle frame.
     *
     * @param frame The to-be-destroyed ThrottleFrame
     */
    public void requestThrottleWindowDestruction(ThrottleWindow frame) {
        if (frame != null) {
            destroyThrottleWindow(frame);
            synchronized (this) {
                throttleWindows.remove(frame);
                if (!throttleWindows.isEmpty()) {
                    requestFocusForNextThrottleWindow();
                }
            }
        }
        throttlesListPanel.getTableModel().fireTableStructureChanged();        
    }

    public synchronized void requestAllThrottleWindowsDestroyed() {
        for (ThrottleControllersUIContainer frame : throttleWindows) {
            destroyThrottleWindow((ThrottleWindow)frame);
        }
        throttleWindows = new ArrayList<>(0);
        throttlesListPanel.getTableModel().fireTableStructureChanged();        
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
    private void destroyThrottleWindow(ThrottleWindow window) {
        throttleWindows.remove(window);
        window.dispose();        
        throttlesListPanel.getTableModel().fireTableStructureChanged();        
    }

    @Override
    public Iterator<ThrottleControllersUIContainer> iterator() {
        return throttleWindows.iterator();
    }
       
    /**
     * Return the number of active thottle windows.
     *
     * @return the number of active thottle window.
     */
    @Override
    public synchronized int getNbThrottleControllersContainers() {
        return throttleWindows.size();
    }
    
    @Override
    public synchronized ThrottleControllersUIContainer getThrottleControllersContainerAt(int n) {
        if (! (n < throttleWindows.size())) {
            return null;
        }
        return throttleWindows.get(n);
    }

    public synchronized void requestFocusForNextThrottleWindow() {
        activeFrame = (activeFrame + 1) % throttleWindows.size();
        ThrottleWindow tw = (ThrottleWindow) throttleWindows.get(activeFrame);
        tw.requestFocus();
        tw.toFront();
    }

    public synchronized void requestFocusForPreviousThrottleWindow() {
        activeFrame--;
        if (activeFrame < 0) {
            activeFrame = throttleWindows.size() - 1;
        }
        ThrottleWindow tw =(ThrottleWindow) throttleWindows.get(activeFrame);
        tw.requestFocus();
        tw.toFront();
    }

    public synchronized ThrottleWindow getCurrentThrottleFrame() {
        if (throttleWindows == null) {
            return null;
        }
        if (throttleWindows.isEmpty()) {
            return null;
        }
        return (ThrottleWindow) throttleWindows.get(activeFrame);
    }

    public ThrottlesListPanel getThrottlesListPanel() {
        if (throttlesListPanel == null) {
            buildThrottleListFrame();
        }
        return throttlesListPanel;
    }

    private void buildThrottleListFrame() {
        throttlesListFrame = new JmriJFrame(Bundle.getMessage("ThrottleListFrameTile"));
        throttlesListPanel = new ThrottlesListPanel();
        throttlesListFrame.setContentPane(throttlesListPanel);
        throttlesListFrame.pack();
    }

    /*
     * Show JMRI native throttle list window
     *
     */
    public void showThrottlesList() {
        if (throttlesListFrame == null) {
            buildThrottleListFrame();
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
    public void applyPreferences() {
        throttleWindows.forEach(tw -> {
            ((ThrottleWindow)tw).applyPreferences();
        });
        throttlesListPanel.applyPreferences();
    }

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
     * Get the number of usages of a particular Loco Address.
     * @param la the Loco Address, can be null.
     * @return 0 if no usages, else number of AddressPanel usages.
     */
    @Override
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
    
    // private final static Logger log = LoggerFactory.getLogger(ThrottleFrameManager.class);
}
