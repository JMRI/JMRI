// MarklinNamedPaneAction.java
package jmri.jmrix.marklin.swing;

import javax.swing.Icon;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to create and load a JmriPanel from just its name.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 * @version	$Revision: 17977 $
 */
public class MarklinNamedPaneAction extends jmri.util.swing.JmriNamedPaneAction {

    /**
     *
     */
    private static final long serialVersionUID = 4877105383231547187L;

    /**
     * Enhanced constructor for placing the pane in various GUIs
     */
    public MarklinNamedPaneAction(String s, WindowInterface wi, String paneClass, MarklinSystemConnectionMemo memo) {
        super(s, wi, paneClass);
        this.memo = memo;
    }

    public MarklinNamedPaneAction(String s, Icon i, WindowInterface wi, String paneClass, MarklinSystemConnectionMemo memo) {
        super(s, i, wi, paneClass);
        this.memo = memo;
    }

    MarklinSystemConnectionMemo memo;

    @Override
    public JmriPanel makePanel() {
        JmriPanel p = super.makePanel();
        if (p == null) {
            return null;
        }

        try {
            ((MarklinPanelInterface) p).initComponents(memo);
            return p;
        } catch (Exception ex) {
            log.warn("could not init pane class: " + paneClass + " due to:" + ex);
            ex.printStackTrace();
        }

        return p;
    }

    private final static Logger log = LoggerFactory.getLogger(MarklinNamedPaneAction.class.getName());
}

/* @(#)MarklinNamedPaneAction.java */
