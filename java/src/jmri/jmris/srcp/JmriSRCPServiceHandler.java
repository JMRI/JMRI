// JmriSRCPServiceHandler.java

package jmri.jmris.srcp;

/**
 * This class provides access to the service handlers for individual object
 * types which can be passed to a parser visitor object.
 *
 * In addition to service handlers handling the connection's services,
 * This class keeps track of connection details the parser visitor
 * may be asked to return. 
 * 
 * @author Paul Bender Copyright (C) 2014
 * @version $Revision$
 *
 */
public class JmriSRCPServiceHandler extends jmri.jmris.ServiceHandler {

     public JmriSRCPServiceHandler(int port) {
        super();
        _session_number = port+(jmri.InstanceManager.timebaseInstance().getTime().getTime());
     }

     public long getSessionNumber() { return _session_number; }

     private long _session_number = 0;


}
