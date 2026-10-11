package jmri.jmrix.bidib;

import java.io.IOException;
import java.util.Set;
import org.bidib.jbidibc.core.BidibInterface;
import org.bidib.jbidibc.core.MessageListener;
import org.bidib.jbidibc.core.NodeListener;
import org.bidib.jbidibc.core.node.listener.TransferListener;
import org.bidib.jbidibc.messages.ConnectionListener;
import org.bidib.jbidibc.messages.helpers.Context;
import org.bidib.jbidibc.messages.helpers.DefaultContext;

/**
 * Abstract base for classes representing a BiDiB communications port
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author Eckart Meyer Copyright (C) 2023
 */
public abstract class BiDiBNetworkPortController extends jmri.jmrix.AbstractNetworkPortController implements BiDiBPortController {

    protected BidibInterface bidib = null;
    protected Context context = new DefaultContext();

    public BiDiBNetworkPortController() {
        super(new BiDiBSystemConnectionMemo());
    }

    @Override
    public abstract void connect(String host, int port) throws IOException;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BiDiBSystemConnectionMemo getSystemConnectionMemo() {
        return (BiDiBSystemConnectionMemo) super.getSystemConnectionMemo();
    }

    // Implementation of the BiDiBPortController interface

    /**
     * Get the physical port name used with jbidibc
     * 
     * @return physical port name
     */
    @Override
    public String getRealPortName() {
        return getCurrentPortName(); //default implemention
    }
    
    /**
     * Register all Listeners to the specific BiDiB Object.
     * We need this here since the BidibInterface does not
     * provide this method.
     * 
     * @param connectionListener add to this
     * @param nodeListeners listeners to add
     * @param messageListeners listeners to add
     * @param transferListeners  listeners to add
     */    
    @Override
    public abstract void registerAllListeners(ConnectionListener connectionListener, Set<NodeListener> nodeListeners,
        Set<MessageListener> messageListeners, Set<TransferListener> transferListeners);
    
    /**
     * Get the Bidib adapter context
     * 
     * @return Context
     */
    @Override
    public Context getContext() {
        return context;
    }
}



