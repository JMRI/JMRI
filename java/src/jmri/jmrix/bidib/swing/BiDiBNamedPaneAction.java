package jmri.jmrix.bidib.swing;

import javax.swing.Icon;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;
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
 * @author Eckart Meyer Copyright (C) 2020
 */
public class BiDiBNamedPaneAction extends jmri.util.swing.JmriNamedPaneAction {

    /**
     * Enhanced constructor for placing the pane in various GUIs
     * @param s Name
     * @param wi Window context
     * @param paneClass class to instantiate
     * @param memo Source of stuff
     */
    public BiDiBNamedPaneAction(String s, WindowInterface wi, String paneClass, BiDiBSystemConnectionMemo memo) {
        super(s, wi, paneClass);
        this.memo = memo;
    }

    public BiDiBNamedPaneAction(String s, Icon i, WindowInterface wi, String paneClass, BiDiBSystemConnectionMemo memo) {
        super(s, i, wi, paneClass);
        this.memo = memo;
    }

    BiDiBSystemConnectionMemo memo;

    @Override
    public JmriPanel makePanel() {
        JmriPanel p = super.makePanel();
        if (p == null) {
            return null;
        }

        try {
            ((BiDiBPanelInterface) p).initComponents(memo);
            return p;
        } catch (Exception ex) {
            log.warn("could not init pane class: {}", paneClass, ex);
        }

        return p;
    }

    private static final Logger log = LoggerFactory.getLogger(BiDiBNamedPaneAction.class);

}
