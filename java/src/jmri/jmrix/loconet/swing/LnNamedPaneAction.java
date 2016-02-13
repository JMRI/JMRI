// LnNamedPaneAction.java
package jmri.jmrix.loconet.swing;

import javax.swing.Icon;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to create and load a JmriPanel from just its name.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 * @version	$Revision$
 */
public class LnNamedPaneAction extends jmri.util.swing.JmriNamedPaneAction {

    /**
     *
     */
    private static final long serialVersionUID = 3189519475374368759L;

    /**
     * Enhanced constructor for placing the pane in various GUIs
     */
    public LnNamedPaneAction(String s, WindowInterface wi, String paneClass, LocoNetSystemConnectionMemo memo) {
        super(s, wi, paneClass);
        this.memo = memo;
    }

    public LnNamedPaneAction(String s, Icon i, WindowInterface wi, String paneClass, LocoNetSystemConnectionMemo memo) {
        super(s, i, wi, paneClass);
        this.memo = memo;
    }

    LocoNetSystemConnectionMemo memo;

    public JmriPanel makePanel() {
        JmriPanel p = super.makePanel();
        if (p == null) {
            return null;
        }

        try {
            ((LnPanelInterface) p).initComponents(memo);
            return p;
        } catch (Exception ex) {
            log.warn("could not init pane class: " + paneClass + " due to:" + ex);
            ex.printStackTrace();
        }

        return p;
    }

    private final static Logger log = LoggerFactory.getLogger(LnNamedPaneAction.class.getName());
}

/* @(#)LnNamedPaneAction.java */
