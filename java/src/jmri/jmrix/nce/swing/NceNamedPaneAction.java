// NceNamedPaneAction.java
package jmri.jmrix.nce.swing;

import javax.swing.Icon;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to create and load a JmriPanel from just its name.
 *
 * @author	Bob Jacobsen Copyright (C) 2010 Copied from LocoNet
 * @author kcameron
 * @version	$Revision$
 */
public class NceNamedPaneAction extends jmri.util.swing.JmriNamedPaneAction {

    /**
     *
     */
    private static final long serialVersionUID = -7955184289782860199L;

    /**
     * Enhanced constructor for placing the pane in various GUIs
     */
    public NceNamedPaneAction(String s, WindowInterface wi, String paneClass, NceSystemConnectionMemo memo) {
        super(s, wi, paneClass);
        this.memo = memo;
    }

    public NceNamedPaneAction(String s, Icon i, WindowInterface wi, String paneClass, NceSystemConnectionMemo memo) {
        super(s, i, wi, paneClass);
        this.memo = memo;
    }

    NceSystemConnectionMemo memo;

    public JmriPanel makePanel() {
        JmriPanel p = super.makePanel();
        if (p == null) {
            return null;
        }

        try {
            ((NcePanelInterface) p).initComponents(memo);
            return p;
        } catch (Exception ex) {
            log.warn("could not init pane class: " + paneClass + " due to:" + ex);
            ex.printStackTrace();
        }

        return p;
    }

    private final static Logger log = LoggerFactory.getLogger(NceNamedPaneAction.class.getName());
}

/* @(#)NceNamedPaneAction.java */
