package jmri.jmrit.throttle;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JFrame;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for allocating and deallocating throttles frames. Not to be
 * confused with ThrottleManager.
 *
 * @author Glen Oberhauser
 */
public class ThrottleFrameManager implements InstanceManagerAutoDefault {

    private int activeFrame;

    private ArrayList<ThrottleWindow> throttleWindows; // synchronized access

    private JmriJFrame throttlePreferencesFrame;
    private JmriJFrame throttlesListFrame;
    private ThrottlesListPanel throttlesListPanel;

    /**
     * Constructor for the ThrottleFrameManager object.
     */
    public ThrottleFrameManager() {
        throttleWindows = new ArrayList<>(0);
        if (jmri.InstanceManager.getNullableDefault(ThrottlesPreferences.class) == null) {
            jmri.InstanceManager.store(new ThrottlesPreferences(), ThrottlesPreferences.class);
        }
        buildThrottleListFrame();
    }

    /**
     * Tell this manager that a new ThrottleWindow was created.
     *
     * @return The newly created ThrottleWindow
     */
    public ThrottleWindow createThrottleWindow() {
        ThrottleWindow tw = new ThrottleWindow();
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
        return createThrottleWindow().getCurrentThrottleFrame();
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
                try {
                    throttleWindows.remove(throttleWindows.indexOf(frame));
                } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
                    log.debug(ex.toString());
                }
                if (throttleWindows.size() > 0) {
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

    public ThrottlesPreferences getThrottlesPreferences() {
        return InstanceManager.getDefault(ThrottlesPreferences.class);
    }

    public ThrottlesListPanel getThrottlesListPanel() {
        return throttlesListPanel;
    }

    private void buildThrottlePreferencesFrame() {
        throttlePreferencesFrame = new JmriJFrame(Bundle.getMessage("ThrottlePreferencesFrameTitle"));
        ThrottlesPreferencesPane tpP = new ThrottlesPreferencesPane();
        throttlePreferencesFrame.add(tpP);
        tpP.setContainer(throttlePreferencesFrame);
        throttlePreferencesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        throttlePreferencesFrame.pack();
    }

    private void buildThrottleListFrame() {
        throttlesListFrame = new JmriJFrame(Bundle.getMessage("ThrottleListFrameTile"));
        throttlesListPanel = new ThrottlesListPanel();
        throttlesListFrame.setContentPane(throttlesListPanel);
        throttlesListFrame.pack();
    }

    public void showThrottlesList() {
        if (throttlesListFrame == null) {
            buildThrottleListFrame();
        }
        throttlesListFrame.setVisible(!throttlesListFrame.isVisible());
    }

    public void showThrottlesPreferences() {
        if (throttlePreferencesFrame == null) {
            buildThrottlePreferencesFrame();
        }
        throttlePreferencesFrame.setVisible(true);
        throttlePreferencesFrame.requestFocus();
    }

    private final static Logger log = LoggerFactory.getLogger(ThrottleFrameManager.class);
}
