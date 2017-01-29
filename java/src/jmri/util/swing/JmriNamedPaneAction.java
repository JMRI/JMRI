package jmri.util.swing;

import javax.swing.Icon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to create and load a JmriPanel from just its name.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class JmriNamedPaneAction extends JmriAbstractAction {

    /**
     * Constructor that associates the panel with the given window.
     *
     * @param s         panel name
     * @param wi        window associated with pane
     * @param paneClass class of the pane
     */
    public JmriNamedPaneAction(String s, WindowInterface wi, String paneClass) {
        super(s, wi);
        this.paneClass = paneClass;
    }

    public JmriNamedPaneAction(String s, Icon i, WindowInterface wi, String paneClass) {
        super(s, i, wi);
        this.paneClass = paneClass;
    }

    /**
     * Original constructor for compatibility with older menus. Assumes SDI GUI.
     *
     * @param s the panel name
     * @param p the panel class
     */
    public JmriNamedPaneAction(String s, String p) {
        this(s, new jmri.util.swing.sdi.JmriJFrameInterface(), p);
    }

    protected String paneClass;

    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        try {
            JmriPanel p = (JmriPanel) Class.forName(paneClass).newInstance();
            p.setWindowInterface(wi);
            p.initComponents();
            p.initContext(context);

            return p;
        } catch (Exception ex) {
            log.warn("could not load pane class: " + paneClass + " due to:" + ex);
            ex.printStackTrace();
            return null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(JmriNamedPaneAction.class.getName());
}
