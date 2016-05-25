// EcosNamedPaneAction.java
package jmri.jmrix.ecos.swing;

import javax.swing.Icon;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
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
public class EcosNamedPaneAction extends jmri.util.swing.JmriNamedPaneAction {

    /**
     *
     */
    private static final long serialVersionUID = 5891812706143295616L;

    /**
     * Enhanced constructor for placing the pane in various GUIs
     */
    public EcosNamedPaneAction(String s, WindowInterface wi, String paneClass, EcosSystemConnectionMemo memo) {
        super(s, wi, paneClass);
        this.memo = memo;
    }

    public EcosNamedPaneAction(String s, Icon i, WindowInterface wi, String paneClass, EcosSystemConnectionMemo memo) {
        super(s, i, wi, paneClass);
        this.memo = memo;
    }

    EcosSystemConnectionMemo memo;

    @Override
    public JmriPanel makePanel() {
        JmriPanel p = super.makePanel();
        if (p == null) {
            return null;
        }

        try {
            ((EcosPanelInterface) p).initComponents(memo);
            return p;
        } catch (Exception ex) {
            log.warn("could not init pane class: " + paneClass + " due to:" + ex);
            ex.printStackTrace();
        }

        return p;
    }

    private final static Logger log = LoggerFactory.getLogger(EcosNamedPaneAction.class.getName());
}

/* @(#)EcosNamedPaneAction.java */
