package jmri.jmrix.tams.swing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import jmri.SystemConnectionMemo;
import jmri.jmrix.swing.SystemConnectionAction;
import jmri.jmrix.tams.TamsSystemConnectionMemo;
import jmri.util.swing.JmriNamedPaneAction;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;

/**
 * Action to create and load a JmriPanel from just its name.
 *
 * Based on work by Bob Jacobsen
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class TamsNamedPaneAction extends JmriNamedPaneAction implements SystemConnectionAction<TamsSystemConnectionMemo> {

    /**
     * Enhanced constructor for placing the pane in various GUIs.
     * @param s action name.
     * @param wi window interface in use.
     * @param paneClass pane class.
     * @param memo system connection.
     */
    public TamsNamedPaneAction(String s, WindowInterface wi, String paneClass, TamsSystemConnectionMemo memo) {
        super(s, wi, paneClass);
        this.memo = memo;
    }

    /**
     * Enhanced constructor for placing the pane in various GUIs.
     * @param s action name.
     * @param i icon to use
     * @param wi window interface in use.
     * @param paneClass pane class.
     * @param memo system connection.
     */
    public TamsNamedPaneAction(String s, Icon i, WindowInterface wi, String paneClass, TamsSystemConnectionMemo memo) {
        super(s, i, wi, paneClass);
        this.memo = memo;
    }

    TamsSystemConnectionMemo memo;

    @Override
    public JmriPanel makePanel() {
        JmriPanel p = super.makePanel();
        if (p == null) {
            return null;
        }

        ((TamsPanelInterface) p).initComponents(memo);
        return p;
    }

    @Override
    public TamsSystemConnectionMemo getSystemConnectionMemo() {
        return memo;
    }

    @Override
    public void setSystemConnectionMemo(TamsSystemConnectionMemo memo) {
        this.memo = memo;
    }

    @Override
    public Set<Class<? extends SystemConnectionMemo>> getSystemConnectionMemoClasses() {
        return new HashSet<>(Arrays.asList(TamsSystemConnectionMemo.class));
    }

    // private final static Logger log = LoggerFactory.getLogger(TamsNamedPaneAction.class);
}
