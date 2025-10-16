package jmri.jmrix.can.swing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;
import jmri.SystemConnectionMemo;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.swing.JmriPanel;
import jmri.jmrix.swing.SystemConnectionAction;
import jmri.util.swing.WindowInterface;

/**
 * Action to create and load a CAN-specific JmriPanel
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class CanNamedPaneAction extends jmri.util.swing.JmriNamedPaneAction 
    implements SystemConnectionAction<CanSystemConnectionMemo> {

    /**
     * Enhanced constructor for placing the pane in various GUIs.
     * @param s Window Name
     * @param wi JmriJFrameInterface
     * @param paneClass Name of class to open
     * @param memo System Connection
     */
    public CanNamedPaneAction(String s, WindowInterface wi, String paneClass, CanSystemConnectionMemo memo) {
        super(s, wi, paneClass);
        this.memo = memo;
    }

    /**
     * Enhanced constructor for placing the pane in various GUIs.
     * @param s Window Name
     * @param i Icon to display
     * @param wi JmriJFrameInterface
     * @param paneClass Name of class to open
     * @param memo System Connection
     */
    public CanNamedPaneAction(String s, Icon i, WindowInterface wi, String paneClass, CanSystemConnectionMemo memo) {
        super(s, i, wi, paneClass);
        this.memo = memo;
    }

    CanSystemConnectionMemo memo;

    /**
     * Makes Panel and calls initComponents
     * {@inheritDoc}
     */
    @Override
    public JmriPanel makePanel() {
        JmriPanel p = super.makePanel();
        if (p == null) {
            return null;
        }

        try {
            ((CanPanelInterface) p).initComponents(memo);
            return p;
        } catch (Exception ex) {
            log.warn("could not init pane class: {}", paneClass, ex);
        }

        return p;
    }

    @Override
    public CanSystemConnectionMemo getSystemConnectionMemo() {
        return this.memo;
    }

    @Override
    public void setSystemConnectionMemo(CanSystemConnectionMemo memo) throws IllegalArgumentException {
        if (CanSystemConnectionMemo.class.isAssignableFrom(memo.getClass())) {
            this.memo = memo;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Set<Class<? extends SystemConnectionMemo>> getSystemConnectionMemoClasses() {
        return new HashSet<>(Arrays.asList(CanSystemConnectionMemo.class));
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CanNamedPaneAction.class);

}
