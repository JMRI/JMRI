package jmri.jmrit.throttle;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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

    private final static int NEXT_THROTTLE_KEY = KeyEvent.VK_RIGHT;
    private final static int PREV_THROTTLE_KEY = KeyEvent.VK_LEFT;

    private int activeFrame;
    private final ThrottleCyclingKeyListener throttleCycler;

    private ArrayList<ThrottleWindow> throttleWindows;

    private JmriJFrame throttlePreferencesFrame;
    private JmriJFrame throttlesListFrame;
    private ThrottlesListPanel throttlesListPanel;

    /**
     * Constructor for the ThrottleFrameManager object
     */
    public ThrottleFrameManager() {
        throttleCycler = new ThrottleCyclingKeyListener();
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
        KeyListenerInstaller.installKeyListenerOnAllComponents(throttleCycler, tw);
        throttleWindows.add(tw);
        activeFrame = throttleWindows.indexOf(tw);
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
            try {
                throttleWindows.remove(throttleWindows.indexOf(frame));
            } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
                log.debug(ex.toString());
            }
            if (throttleWindows.size() > 0) {
                requestFocusForNextFrame();
            }
        }
    }

    public void requestAllThrottleWindowsDestroyed() {
        for (Iterator<ThrottleWindow> i = throttleWindows.iterator(); i.hasNext();) {
            ThrottleWindow frame = i.next();
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
    public Iterator<ThrottleWindow> getThrottleWindows() {
        return throttleWindows.iterator();
    }

    public int getNumberThrottleWindows() {
        return throttleWindows.size();
    }

    private void requestFocusForNextFrame() {
        activeFrame = (activeFrame + 1) % throttleWindows.size();
        ThrottleWindow tf = throttleWindows.get(activeFrame);
        tf.requestFocus();
        tf.toFront();
    }

    private void requestFocusForPreviousFrame() {
        activeFrame--;
        if (activeFrame < 0) {
            activeFrame = throttleWindows.size() - 1;
        }
        ThrottleWindow tf = throttleWindows.get(activeFrame);
        tf.requestFocus();
        tf.toFront();
    }

    public ThrottleWindow getCurrentThrottleFrame() {
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

    /**
     * Description of the Class
     *
     * @author glen
     */
    class ThrottleCyclingKeyListener extends KeyAdapter {

        /**
         * Description of the Method
         *
         * @param e Description of the Parameter
         */
        @Override
        public void keyReleased(KeyEvent e) {
            if (e.isShiftDown() && e.getKeyCode() == NEXT_THROTTLE_KEY) {
                requestFocusForNextFrame();
            } else if (e.isShiftDown() && e.getKeyCode() == PREV_THROTTLE_KEY) {
                requestFocusForPreviousFrame();
            }
        }
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
