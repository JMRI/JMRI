package jmri.jmrix.loconet.swing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.swing.SystemConnectionAction;
import jmri.util.swing.JmriNamedPaneAction;
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
public class LnNamedPaneAction extends JmriNamedPaneAction implements SystemConnectionAction {

    /**
     *
     */
    private static final long serialVersionUID = 3189519475374368759L;

    /**
     * Enhanced constructor for placing the pane in various GUIs
     */
    public LnNamedPaneAction(String s, WindowInterface wi, String paneClass, LocoNetSystemConnectionMemo memo) {
        super(s, wi, paneClass);
        this.memo = memo;
    }

    public LnNamedPaneAction(String s, Icon i, WindowInterface wi, String paneClass, LocoNetSystemConnectionMemo memo) {
        super(s, i, wi, paneClass);
        this.memo = memo;
    }

    LocoNetSystemConnectionMemo memo;

    @Override
    public JmriPanel makePanel() {
        JmriPanel p = super.makePanel();
        if (p == null) {
            return null;
        }

        try {
            if (LnPanelInterface.class.isAssignableFrom(p.getClass())) {
                ((LnPanelInterface) p).initComponents(memo);
            }
            return p;
        } catch (Exception ex) {
            log.warn("Could not initialize class \"{}\"", paneClass, ex);
        }

        return p;
    }

    private final static Logger log = LoggerFactory.getLogger(LnNamedPaneAction.class.getName());

    @Override
    public SystemConnectionMemo getSystemConnectionMemo() {
        return this.memo;
    }

    @Override
    public void setSystemConnectionMemo(SystemConnectionMemo memo) throws IllegalArgumentException {
        if (LocoNetSystemConnectionMemo.class.isAssignableFrom(memo.getClass())) {
            this.memo = (LocoNetSystemConnectionMemo) memo;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Set<Class<? extends SystemConnectionMemo>> getSystemConnectionMemoClasses() {
        return new HashSet<>(Arrays.asList(LocoNetSystemConnectionMemo.class));
    }
}
