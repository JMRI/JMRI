package jmri.jmrix.can.cbus.node;

import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
    
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Class to represent a node imported from FCU file.
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeFromFcu extends CbusNode {
    
    public CbusNodeFromFcu ( CanSystemConnectionMemo connmemo, int nodenumber ){
        super( connmemo, nodenumber );  
    }
    
    // does nothing
    @Override
    public void message(CanMessage m) {
    }
    
    // does nothing
    @Override
    public void reply(CanReply m) {
    }
    
    @Override
    public void dispose(){
    }
    
    // private static final Logger log = LoggerFactory.getLogger(CbusNodeFromFcu.class);
    
}
