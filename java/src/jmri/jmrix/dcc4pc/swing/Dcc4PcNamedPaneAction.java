package jmri.jmrix.dcc4pc.swing;

import javax.swing.Icon;
import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to create and load a JmriPanel from just its name.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class Dcc4PcNamedPaneAction extends jmri.util.swing.JmriNamedPaneAction {

    /**
     * Enhanced constructor for placing the pane in various GUIs
     */
    public Dcc4PcNamedPaneAction(String s, WindowInterface wi, String paneClass, Dcc4PcSystemConnectionMemo memo) {
        super(s, wi, paneClass);
        this.memo = memo;
    }

    public Dcc4PcNamedPaneAction(String s, Icon i, WindowInterface wi, String paneClass, Dcc4PcSystemConnectionMemo memo) {
        super(s, i, wi, paneClass);
        this.memo = memo;
    }

    Dcc4PcSystemConnectionMemo memo;

    @Override
    public JmriPanel makePanel() {
        JmriPanel p = super.makePanel();
        if (p == null) {
            return null;
        }

        try {
            ((Dcc4PcPanelInterface) p).initComponents(memo);
            return p;
        } catch (Exception ex) {
            log.warn("could not init pane class: {}", paneClass, ex);
        }

        return p;
    }

    private final static Logger log = LoggerFactory.getLogger(Dcc4PcNamedPaneAction.class);
}
