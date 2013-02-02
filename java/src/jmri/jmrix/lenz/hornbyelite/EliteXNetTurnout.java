/**
 * EliteXNetTurnout.java
 *
 * Description:		extend jmri.jmrix.XNetTurnout to handle turnouts on
 *                      Hornby Elite connections.
 *                      See XNetTurnout for further documentation.
 * </P>
 * @author			Paul Bender Copyright (C) 2008 
 * @version			$Revision$
 */

package jmri.jmrix.lenz.hornbyelite;

import org.apache.log4j.Logger;
import jmri.jmrix.lenz.XNetTrafficController;

public class EliteXNetTurnout extends jmri.jmrix.lenz.XNetTurnout {

    public EliteXNetTurnout(String prefix,int pNumber,XNetTrafficController tc) {  // a human-readable turnout number must be specified!
        super(prefix,pNumber,tc);
        mNumber=pNumber+1;  // The Elite has an off by 1 error.  What the 
                            // protocol says should be address 2 is address 
                            // 1 on the Elite.
    }

   /* Send an "Off" message to the decoder for this output  */
    protected synchronized void sendOffMessage() {
               // The Elite appears to react to the on and off messages
               // in the same manner, and does not handle feedback properly
               // Set the known state to the command state and the internalState               // to idlestate.
               newKnownState(getCommandedState());
               internalState = jmri.jmrix.lenz.XNetTurnout.IDLE;
    }



    static Logger log = Logger.getLogger(EliteXNetTurnout.class.getName());

}


/* @(#)EliteXNetTurnout.java */

