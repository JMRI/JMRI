package jmri.jmrix.jmriclient;

/**
 * Abstract base for classes representing a JMRIClient communications port
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2010
 * @author Paul Bender Copyright (C) 2010
 */
public abstract class JMRIClientPortController extends jmri.jmrix.AbstractNetworkPortController {

    // base class. Implementations will provide InputStream and OutputStream
    // objects to JMRIClientTrafficController classes, who in turn will deal in messages.
    protected JMRIClientPortController(JMRIClientSystemConnectionMemo connectionMemo) {
        super(connectionMemo);
        allowConnectionRecovery=true;
        setConnectionTimeout(30000);
    }

    @Override
    public JMRIClientSystemConnectionMemo getSystemConnectionMemo() {
        return (JMRIClientSystemConnectionMemo) super.getSystemConnectionMemo();
    }

    @Override
    protected void resetupConnection() {
       // reconnect the port to the traffic controller.
       getSystemConnectionMemo().getJMRIClientTrafficController().connectPort(this);
       // notify the memo that we've restarted, so it can ask the associated 
       // managers to refresh status
       getSystemConnectionMemo().requestAllStatus();       
    }

}
