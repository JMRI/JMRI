/**
 * EliteXNetTurnout.java
 *
 * Description:		extend jmri.jmrix.XNetTurnout to handle turnouts on
 *                      Hornby Elite connections.
 *                      See XNetTurnout for further documentation.
 * </P>
 * @author			Paul Bender Copyright (C) 2008 
 * @version			$Revision: 1.1 $
 */

package jmri.jmrix.lenz.hornbyelite;

import jmri.AbstractTurnout;

public class EliteXNetTurnout extends jmri.jmrix.lenz.XNetTurnout {

    public EliteXNetTurnout(int pNumber) {  // a human-readable turnout number must be specified!
        super(pNumber);
        mNumber=pNumber+1;  // The Elite has an off by 1 error.  What the 
                            // protocol says should be address 2 is address 
                            // 1 on the Elite.
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EliteXNetTurnout.class.getName());

}


/* @(#)EliteXNetTurnout.java */

