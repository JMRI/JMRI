// PowerlineNamedPaneAction.java
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
 * @author	Bob Jacobsen Copyright (C) 2010 Copied from NCE Converted to multiple
 * connection
 * @author kcameron Copyright (C) 2011
 * @version	$Revision$
 */
public class PowerlineNamedPaneAction extends jmri.util.swing.JmriNamedPaneAction {

    /**
     *
     */
    private static final long serialVersionUID = 3525473344131355146L;

    /**
     * Enhanced constructor for placing the pane in various GUIs
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

    public JmriPanel makePanel() {
        JmriPanel p = super.makePanel();
        if (p == null) {
            return null;
        }

        try {
            ((PowerlinePanelInterface) p).initComponents(memo);
            return p;
        } catch (Exception ex) {
            log.warn("could not init pane class: " + paneClass + " due to:" + ex);
            ex.printStackTrace();
        }

        return p;
    }

    private final static Logger log = LoggerFactory.getLogger(PowerlineNamedPaneAction.class.getName());
}

/* @(#)PowerlineNamedPaneAction.java */
