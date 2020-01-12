package jmri.jmrix.rfid.swing;

import javax.swing.Icon;
import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to create and load a JmriPanel from just its name.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public class RfidNamedPaneAction extends jmri.util.swing.JmriNamedPaneAction {

    /**
     * Enhanced constructor for placing the pane in various GUIs
     */
    public RfidNamedPaneAction(String s, WindowInterface wi, String paneClass, RfidSystemConnectionMemo memo) {
        super(s, wi, paneClass);
        this.memo = memo;
    }

    public RfidNamedPaneAction(String s, Icon i, WindowInterface wi, String paneClass, RfidSystemConnectionMemo memo) {
        super(s, i, wi, paneClass);
        this.memo = memo;
    }

    RfidSystemConnectionMemo memo;

    @Override
    public JmriPanel makePanel() {
        JmriPanel p = super.makePanel();
        if (p == null) {
            return null;
        }

        try {
            ((RfidPanelInterface) p).initComponents(memo);
            return p;
        } catch (Exception ex) {
            log.warn("could not init pane class: {}", paneClass, ex);
        }

        return p;
    }

    private static final Logger log = LoggerFactory.getLogger(RfidNamedPaneAction.class);

}
