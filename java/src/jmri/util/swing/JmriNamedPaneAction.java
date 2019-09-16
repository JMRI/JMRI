package jmri.util.swing;

import javax.swing.Icon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action that, when invoked, creates a JmriPanel from its class name
 * and installs it in a given window.
 * <p>
 * Windows are referenced through the {@link WindowInterface}, which can
 * provide access to a new or existing single-pane window, or a more complex multi-pane
 * window as seen in the DecoderPro interface.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class JmriNamedPaneAction extends JmriAbstractAction {

    /**
     * Constructor that associates a newly created panel with the given window, showing a name
     *
     * @param s         Human-readable panel name for display by the action
     * @param wi        Window into which to install the new panel. If you want it to be put into a existing
     *                  one, provide a reference. To create a new window
     *                  containing just this pane, use "new jmri.util.swing.sdi.JmriJFrameInterface()"
     * @param paneClass Name of the panel's class, which must be a subclass of JmriPanel. That's not
     *                  checked at compile time or when the constructor runs, but must be true
     *                  for the action to be invoked successfully.
     */
    public JmriNamedPaneAction(String s, WindowInterface wi, String paneClass) {
        super(s, wi);
        this.paneClass = paneClass;
    }

    /**
     * Constructor that associates a newly created panel with the given window, showing a name and icon
     *
     * @param s         Human-readable panel name for display by the action
     * @param i         Icon for display by the action
     * @param wi        Window into which to install the new panel. If you want it to be put into a existing
     *                  one, provide a reference. To create a new window
     *                  containing just this pane, use "new jmri.util.swing.sdi.JmriJFrameInterface()"
     * @param paneClass Name of the panel's class, which must be a subclass of JmriPanel. That's not
     *                  checked at compile time or when the constructor runs, but must be true
     *                  for the action to be invoked successfully.
     */
    public JmriNamedPaneAction(String s, Icon i, WindowInterface wi, String paneClass) {
        super(s, i, wi);
        this.paneClass = paneClass;
    }

    /**
     * Original constructor for compatibility with older menus. Assumes SDI GUI.
     *
     * @param s         Human-readable panel name for display by the action
     * @param paneClass Name of the panel's class, which must be a subclass of JmriPanel. That's not
     *                  checked at compile time or when the constructor runs, but must be true
     *                  for the action to be invoked successfully.
     */
    public JmriNamedPaneAction(String s, String paneClass) {
        this(s, new jmri.util.swing.sdi.JmriJFrameInterface(), paneClass);
    }

    protected String paneClass;

    /**
     * Invoked as part of the action being invoked, e.g. when button pressed
     * or menu item selected, this runs the panel through the initial part of
     * its life cycle and installs in the given window interface.
     * <p>
     * It different or additional initialization is needed, inherit from this class and
     * override this method to do it.
     */
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        try {
            JmriPanel p = (JmriPanel) Class.forName(paneClass).getDeclaredConstructor().newInstance();
            p.setWindowInterface(wi);
            p.initComponents();
            p.initContext(context);

            return p;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | java.lang.reflect.InvocationTargetException ex ) {
            log.warn("could not load pane class: {}", paneClass, ex);
            return null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(JmriNamedPaneAction.class);
}
