package jmri.jmrix.powerline.swing;

import javax.swing.Icon;
import jmri.jmrix.powerline.SerialSystemConnectionMemo;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to create and load a JmriPanel from just its name.
 *
 * @author Bob Jacobsen Copyright (C) 2010 Copied from NCE Converted to multiple
 * connection
 * @author kcameron Copyright (C) 2011
 */
public class PowerlineNamedPaneAction extends jmri.util.swing.JmriNamedPaneAction {

    /**
     * Enhanced constructor for placing the pane in various GUIs
     * @param s         Human-readable panel name for display by the action
     * @param wi        Window into which to install the new panel. If you want it to be put into a existing
     *                  one, provide a reference. To create a new window
     *                  containing just this pane, use "new jmri.util.swing.sdi.JmriJFrameInterface()"
     * @param paneClass Name of the panel's class, which must be a subclass of JmriPanel. That's not
     *                  checked at compile time or when the constructor runs, but must be true
     *                  for the action to be invoked successfully.
     * @param memo      Connection details memo
     */
    public PowerlineNamedPaneAction(String s, WindowInterface wi, String paneClass, SerialSystemConnectionMemo memo) {
        super(s, wi, paneClass);
        this.memo = memo;
    }

    public PowerlineNamedPaneAction(String s, Icon i, WindowInterface wi, String paneClass, SerialSystemConnectionMemo memo) {
        super(s, i, wi, paneClass);
        this.memo = memo;
    }

    SerialSystemConnectionMemo memo;

    @Override
    public JmriPanel makePanel() {
        JmriPanel p = super.makePanel();
        if (p == null) {
            return null;
        }

        try {
            ((PowerlinePanelInterface) p).initComponents(memo);
            return p;
        } catch (Exception ex) {
            log.warn("could not init pane class: {}", paneClass, ex);
        }

        return p;
    }

    private final static Logger log = LoggerFactory.getLogger(PowerlineNamedPaneAction.class);

}
