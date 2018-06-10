package jmri.jmrix.loconet.locormi;

import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.swing.AbstractAction;
import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.swing.SystemConnectionAction;
import jmri.util.zeroconf.ZeroConfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Start a LnMessageServer that will listen for clients wanting to use the
 * LocoNet connection on this machine.
 *
 * @author Alex Shepherd Copyright (c) 2002
 */
public class LnMessageServerAction
        extends AbstractAction implements SystemConnectionAction {

    public LnMessageServerAction(String s) {
        super(s);
    }

    public LnMessageServerAction() {
        this(Bundle.getMessage("MenuItemStartLocoNetServer"));
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
        try {
            // start server
            LnMessageServer server = LnMessageServer.getInstance();
            server.enable();
            // advertise under zeroconf
            ZeroConfService.create("_jmri-locormi._tcp.local.", 1099).publish();
            // disable action, as already run
            setEnabled(false);
        } catch (RemoteException ex) {
            log.warn("LnMessageServerAction Exception: {}", ex.getMessage()); // NOI18N
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LnMessageServerAction.class);

}
