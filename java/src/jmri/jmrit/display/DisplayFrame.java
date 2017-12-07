package jmri.jmrit.display;

import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisplayFrame extends JmriJFrame {

    /**
     * Create a JmriJFrame with standard settings, optional save/restore of size
     * and position.
     *
     * @param saveSize     set true to save the last known size
     * @param savePosition set true to save the last known location
     */
    public DisplayFrame(boolean saveSize, boolean savePosition) {
        super(saveSize, savePosition);
    }

    /**
     * Create a JmriJFrame with with given name plus standard settings, including
     * optional save/restore of size and position.
     *
     * @param name         title of the Frame
     * @param saveSize     set true to save the last knowm size
     * @param savePosition set true to save the last known location
     */
    public DisplayFrame(String name, boolean saveSize, boolean savePosition) {
        super(name, saveSize, savePosition);
    }

    /**
     * Create a JmriJFrame with standard settings, including saving/restoring of
     * size and position.
     */
    public DisplayFrame() {
        this(true, true);
    }

    /**
     * Create a JmriJFrame with with given name plus standard settings, including
     * saving/restoring of size and position.
     *
     * @param name title of the JFrame
     */
    public DisplayFrame(String name) {
        this(name, true, true);
    }

    /**
     * Shared setting for preview pane background color, starts as 0 = use current Panel bg color.
     */
    protected int previewBgSet = 0;

    public void setPreviewBg(int index) {
        previewBgSet = index;
        log.debug("prev set to {}", index);
    }

    public int getPreviewBg() {
        return previewBgSet;
    }

    protected jmri.jmrit.display.palette.InitEventListener listener;
    /**
     * Register display of a different tab. Used on {@link jmri.jmrit.display.palette.ItemPanel}
     *
     * @param listener to attach
     */
    public void setInitEventListener(jmri.jmrit.display.palette.InitEventListener listener) {
        log.debug("listener attached");
        this.listener = listener;
    }

    private final static Logger log = LoggerFactory.getLogger(DisplayFrame.class);

}