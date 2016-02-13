// MrcNamedPaneAction.java
package jmri.jmrix.mrc.swing;

import javax.swing.Icon;
import jmri.jmrix.mrc.MrcSystemConnectionMemo;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to create and load a JmriPanel from just its name.
 *
 * @author	Bob Jacobsen Copyright (C) 2010 Copied from nce.swing
 * @author Ken Cameron 2014
 * @author Kevin Dickerson 2014
 * @version	$Revision: 22942 $
 */
public class MrcNamedPaneAction extends jmri.util.swing.JmriNamedPaneAction {

    /**
     *
     */
    private static final long serialVersionUID = -7955184289782860199L;

    /**
     * Enhanced constructor for placing the pane in various GUIs
     */
    public MrcNamedPaneAction(String s, WindowInterface wi, String paneClass, MrcSystemConnectionMemo memo) {
        super(s, wi, paneClass);
        this.memo = memo;
    }

    public MrcNamedPaneAction(String s, Icon i, WindowInterface wi, String paneClass, MrcSystemConnectionMemo memo) {
        super(s, i, wi, paneClass);
        this.memo = memo;
    }

    MrcSystemConnectionMemo memo;

    public JmriPanel makePanel() {
        JmriPanel p = super.makePanel();
        if (p == null) {
            return null;
        }

        try {
            ((MrcPanelInterface) p).initComponents(memo);
            return p;
        } catch (Exception ex) {
            log.warn("could not init pane class: " + paneClass + " due to:" + ex); //IN18N
            ex.printStackTrace();
        }

        return p;
    }

    private final static Logger log = LoggerFactory.getLogger(MrcNamedPaneAction.class.getName());
}

/* @(#)MrcNamedPaneAction.java */
