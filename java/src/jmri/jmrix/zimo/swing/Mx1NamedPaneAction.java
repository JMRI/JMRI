package jmri.jmrix.zimo.swing;

import javax.swing.Icon;
import jmri.jmrix.zimo.Mx1SystemConnectionMemo;
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
 */
public class Mx1NamedPaneAction extends jmri.util.swing.JmriNamedPaneAction {

    /**
     * Enhanced constructor for placing the pane in various GUIs
     */
    public Mx1NamedPaneAction(String s, WindowInterface wi, String paneClass, Mx1SystemConnectionMemo memo) {
        super(s, wi, paneClass);
        this.memo = memo;
    }

    public Mx1NamedPaneAction(String s, Icon i, WindowInterface wi, String paneClass, Mx1SystemConnectionMemo memo) {
        super(s, i, wi, paneClass);
        this.memo = memo;
    }

    Mx1SystemConnectionMemo memo;

    public JmriPanel makePanel() {
        JmriPanel p = super.makePanel();
        if (p == null) {
            return null;
        }

        try {
            ((Mx1PanelInterface) p).initComponents(memo);
            return p;
        } catch (Exception ex) {
            log.warn("could not init pane class: " + paneClass + " due to:" + ex); //IN18N
            ex.printStackTrace();
        }

        return p;
    }

    private final static Logger log = LoggerFactory.getLogger(Mx1NamedPaneAction.class.getName());
}
