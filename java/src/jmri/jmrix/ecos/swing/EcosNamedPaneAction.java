package jmri.jmrix.ecos.swing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import jmri.SystemConnectionMemo;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.jmrix.swing.SystemConnectionNamedPaneAction;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to create and load a JmriPanel from just its name.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class EcosNamedPaneAction extends SystemConnectionNamedPaneAction<EcosSystemConnectionMemo> {

    /**
     * Enhanced constructor for placing the pane in various GUIs.
     * @param s action title string.
     * @param wi window interface.
     * @param paneClass pane class string.
     * @param memo system connection.
     */
    public EcosNamedPaneAction(String s, WindowInterface wi, String paneClass, EcosSystemConnectionMemo memo) {
        super(s, wi, paneClass, memo);
        this.memo = memo;
    }

    public EcosNamedPaneAction(String s, Icon i, WindowInterface wi, String paneClass, EcosSystemConnectionMemo memo) {
        super(s, i, wi, paneClass, memo);
        this.memo = memo;
    }

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
            log.warn("could not init pane class: {}", paneClass, ex);
        }

        return p;
    }

    private final static Logger log = LoggerFactory.getLogger(EcosNamedPaneAction.class);

    @Override
    public Set<Class<? extends SystemConnectionMemo>> getSystemConnectionMemoClasses() {
        return new HashSet<>(Arrays.asList(EcosSystemConnectionMemo.class));
    }

}
