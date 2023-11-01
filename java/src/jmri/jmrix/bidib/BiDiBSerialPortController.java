package jmri.jmrix.bidib;

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
 * <p>
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author Eckart Meyer Copyright (C) 2019-2023
 */
public abstract class BiDiBSerialPortController extends jmri.jmrix.AbstractSerialPortController implements BiDiBPortController {

    protected BidibInterface bidib = null;
    protected Context context = new DefaultContext();

    public BiDiBSerialPortController() {
        super(new BiDiBSystemConnectionMemo());
    }

    // Implementation of the BiDiBPortController interface
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BiDiBSystemConnectionMemo getSystemConnectionMemo() {
        return (BiDiBSystemConnectionMemo) super.getSystemConnectionMemo();
    }

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
     * @param connectionListener where to add
     * @param nodeListeners listeners to add
     * @param messageListeners listeners to add
     * @param transferListeners listeners to add
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



