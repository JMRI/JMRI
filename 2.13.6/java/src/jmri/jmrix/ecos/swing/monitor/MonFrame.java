// MonFrame.java

package jmri.jmrix.ecos.swing.monitor;

import jmri.jmrix.ecos.*;

/** 
 * Frame displaying (and logging) ECOS command messages
 * @author			Bob Jacobsen   Copyright (C) 2001,2008
 * @version			$Revision$
  * @deprecated 2.11.3
 */
@Deprecated
public class MonFrame extends jmri.jmrix.AbstractMonFrame implements EcosListener {

	public MonFrame(EcosTrafficController tc) {
		super();
                this.tc = tc;
	}

        EcosTrafficController tc;

	protected String title() { return "ECOS Command Monitor"; }
	
	protected void init() {
		// connect to TrafficController
		tc.addEcosListener(this);
	}
  
	public void dispose() {
		tc.removeEcosListener(this);
		super.dispose();
	}
			
	public synchronized void message(EcosMessage l) {  // receive a message and log it
        if (l.isBinary())
          	nextLine("binary cmd: "+l.toString()+"\n", null);
        else
            nextLine("cmd: \""+l.toString()+"\"\n", null);

	}

	public synchronized void reply(EcosReply l) {  // receive a reply message and log it
	    String raw = "";
	    for (int i=0;i<l.getNumDataElements(); i++) {
	        if (i>0) raw+=" ";
            raw = jmri.util.StringUtil.appendTwoHexFromInt(l.getElement(i)&0xFF, raw);
        }
	        
	    if (l.isUnsolicited()) {    
            nextLine("msg: \""+l.toString()+"\"\n", raw);
        } else {
            nextLine("rep: \""+l.toString()+"\"\n", raw);
        }
	}
	
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MonFrame.class.getName());

}
