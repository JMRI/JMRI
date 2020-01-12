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
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class LnNamedPaneAction extends JmriNamedPaneAction implements SystemConnectionAction {

    /**
     * Enhanced constructor for placing the pane in various GUIs.
     *
     * @param s         Human-readable panel name for display by the action
     * @param wi        Window into which to install the new panel. If you want it to be put into a existing
     *                  one, provide a reference. To create a new window
     *                  containing just this pane, use "new jmri.util.swing.sdi.JmriJFrameInterface()"
     * @param paneClass Name of the panel's class, which must be a subclass of JmriPanel. That's not
     *                  checked at compile time or when the constructor runs, but must be true
     *                  for the action to be invoked successfully.
     * @param memo      {@link jmri.jmrix.loconet.LocoNetSystemConnectionMemo} to be used by this object
     */
    public LnNamedPaneAction(String s, WindowInterface wi, String paneClass, LocoNetSystemConnectionMemo memo) {
        super(s, wi, paneClass);
        this.memo = memo;
    }


    /**
     * Enhanced constructor for placing the pane in various GUIs.
     *
     * @param s         Human-readable panel name for display by the action
     * @param i         Icon for display by the action
     * @param wi        Window into which to install the new panel. If you want it to be put into a existing
     *                  one, provide a reference. To create a new window
     *                  containing just this pane, use "new jmri.util.swing.sdi.JmriJFrameInterface()"
     * @param paneClass Name of the panel's class, which must be a subclass of JmriPanel. That's not
     *                  checked at compile time or when the constructor runs, but must be true
     *                  for the action to be invoked successfully.
     * @param memo      {@link jmri.jmrix.loconet.LocoNetSystemConnectionMemo} to be used by this object
     */


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

    @Override
    public SystemConnectionMemo getSystemConnectionMemo() {
        return this.memo;
    }

    @Override
    public void setSystemConnectionMemo(SystemConnectionMemo memo) throws IllegalArgumentException {
        if (LocoNetSystemConnectionMemo.class.isAssignableFrom(memo.getClass())) {
            if (memo instanceof LocoNetSystemConnectionMemo) {
            this.memo = (LocoNetSystemConnectionMemo) memo;
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Set<Class<? extends SystemConnectionMemo>> getSystemConnectionMemoClasses() {
        return new HashSet<>(Arrays.asList(LocoNetSystemConnectionMemo.class));
    }

    private final static Logger log = LoggerFactory.getLogger(LnNamedPaneAction.class);

}
