package jmri.jmrix.loconet.loconetovertcp;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.swing.AbstractAction;
import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.swing.SystemConnectionAction;

/**
 * Implementation of the LocoNet over TCP Server Protocol.
 *
 * @author Alex Shepherd Copyright (C) 2006
 */
public class LnTcpServerAction
        extends AbstractAction implements SystemConnectionAction {

    /**
     * Initiate a LnTcpServer for a given SystemConnectionMemo, eg. when called from a specific system connection menu.
     * TODO allow multiple server instance for different connections
     *
     * @param s name for the server action
     * @param memo the SystemConnectionMemo to connect to
     */
    public LnTcpServerAction(String s, LocoNetSystemConnectionMemo memo) {
        super(s);
        setSystemConnectionMemo(memo);
    }

    /**
     * Initiate a LnTcpServer without a known SystemConnectionMemo, eg. as part of the auto startupAction.
     *
     * @param s name for the server action
     */
    public LnTcpServerAction(String s) {
        this(s, jmri.InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
    }

    public LnTcpServerAction() {
        this(Bundle.getMessage("ServerAction"));
    }

    private LocoNetSystemConnectionMemo memo;

    /**
     * Get the {@link jmri.jmrix.SystemConnectionMemo} this action is bound to.
     *
     * @return the SystemConnectionMemo or null if not bound
     */
    @CheckForNull
    @Override
    public SystemConnectionMemo getSystemConnectionMemo(){
        return memo;
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

    @Override
    public void actionPerformed(ActionEvent e) {
        LnTcpServer.getDefault().enable();
        if (!GraphicsEnvironment.isHeadless()) {
            LnTcpServerFrame.getDefault().setVisible(true);
        }
    }

}
