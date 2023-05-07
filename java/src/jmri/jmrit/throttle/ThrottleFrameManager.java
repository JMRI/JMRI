package jmri.jmrit.throttle;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;

import jmri.InstanceManagerAutoDefault;
import jmri.util.JmriJFrame;

import org.jdom2.Element;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Interface for allocating and deallocating throttles frames. Not to be
 * confused with ThrottleManager.
 *
 * @author Glen Oberhauser
 */
public class ThrottleFrameManager implements InstanceManagerAutoDefault {

    private int activeFrame;

    private ArrayList<ThrottleWindow> throttleWindows; // synchronized access

    private ThrottlesPreferencesWindow throttlePreferencesFrame;
    private JmriJFrame throttlesListFrame;
    private ThrottlesListPanel throttlesListPanel;

    /**
     * Constructor for the ThrottleFrameManager object.
     */
    public ThrottleFrameManager() {
        throttleWindows = new ArrayList<>(0);
        buildThrottleListFrame();
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
    }

    public synchronized void requestAllThrottleWindowsDestroyed() {
        for (ThrottleWindow frame : throttleWindows) {
            destroyThrottleWindow(frame);
        }
        throttleWindows = new ArrayList<>(0);
    }

    /**
     * Perform the destruction of a ThrottleFrame. This method will not affect
     * the throttleFrames list, thus ensuring no synchronozation problems.
     *
     * @param window The ThrottleFrame to be destroyed.
     */
    private void destroyThrottleWindow(ThrottleWindow window) {
        window.dispose();
    }

    /**
     * Retrieve an Iterator over all the ThrottleFrames in existence.
     *
     * @return The Iterator on the list of ThrottleFrames.
     */
    public synchronized Iterator<ThrottleWindow> getThrottleWindows() {
        return throttleWindows.iterator();
    }

    public synchronized int getNumberThrottleWindows() {
        return throttleWindows.size();
    }

    public synchronized void requestFocusForNextThrottleWindow() {
        activeFrame = (activeFrame + 1) % throttleWindows.size();
        ThrottleWindow tw = throttleWindows.get(activeFrame);
        tw.requestFocus();
        tw.toFront();
    }

    public synchronized void requestFocusForPreviousThrottleWindow() {
        activeFrame--;
        if (activeFrame < 0) {
            activeFrame = throttleWindows.size() - 1;
        }
        ThrottleWindow tw = throttleWindows.get(activeFrame);
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
        return throttleWindows.get(activeFrame);
    }

    public ThrottlesListPanel getThrottlesListPanel() {
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
        throttleWindows.forEach(frame -> {
            frame.applyPreferences();
        });
        throttlesListPanel.applyPreferences();
    }

    // private final static Logger log = LoggerFactory.getLogger(ThrottleFrameManager.class);
}
