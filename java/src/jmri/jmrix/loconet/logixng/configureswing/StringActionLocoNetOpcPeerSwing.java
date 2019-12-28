package jmri.jmrix.loconet.logixng.configureswing;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.StringActionManager;
import jmri.jmrit.logixng.string.actions.configureswing.AbstractActionSwing;
import jmri.jmrix.loconet.logixng.StringActionLocoNetOpcPeer;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 */
public class StringActionLocoNetOpcPeerSwing extends AbstractActionSwing {

    @Override
    protected void createPanel(Base object) {
        panel = new JPanel();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull StringBuilder errorMessage) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        StringActionLocoNetOpcPeer action = new StringActionLocoNetOpcPeer(systemName, userName);
        return InstanceManager.getDefault(StringActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("StringActionLocoNet_OPC_PEER_Short");
    }
    
    @Override
    public void dispose() {
    }
    
}
