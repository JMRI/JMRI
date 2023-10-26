package jmri.jmrix.bidib;

import java.util.Set;
import org.bidib.jbidibc.core.MessageListener;
import org.bidib.jbidibc.core.NodeListener;
import org.bidib.jbidibc.core.node.listener.TransferListener;
import org.bidib.jbidibc.messages.ConnectionListener;
import org.bidib.jbidibc.messages.helpers.Context;

/**
 *
 * @author Eckart Meyer Copyright (C) 2023
 */
public interface BiDiBPortController extends jmri.jmrix.PortAdapter {

    /**
     * Get the physical port name used with jbidibc
     * 
     * @return physical port name
     */
    public String getRealPortName();
    
    /**
     * Register all Listeners to the specific BiDiB Object.
     * We need this here since the BidibInterface does not
     * provide this method.
     * 
     * @param connectionListener register to this
     * @param nodeListeners listeners to add 
     * @param messageListeners listeners to add 
     * @param transferListeners  listeners to add 
     */    
    public abstract void registerAllListeners(ConnectionListener connectionListener, Set<NodeListener> nodeListeners,
        Set<MessageListener> messageListeners, Set<TransferListener> transferListeners);
    
    /**
     * Get the Bidib adapter context
     * 
     * @return Context
     */
    public Context getContext();    
}
