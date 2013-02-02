// MrcMonFrame.java

package jmri.jmrix.mrc.mrcmon;

import org.apache.log4j.Logger;
import jmri.jmrix.mrc.MrcListener;
import jmri.jmrix.mrc.MrcMessage;
import jmri.jmrix.mrc.MrcReply;
import jmri.jmrix.mrc.MrcTrafficController;

/**
 * Frame displaying (and logging) MRC command messages
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision$
 */
public class MrcMonFrame extends jmri.jmrix.AbstractMonFrame implements MrcListener {

	public MrcMonFrame() {
		super();
	}

	protected String title() { return "MRC Command Monitor"; }

	protected void init() {
		// connect to TrafficController
		MrcTrafficController.instance().addMrcListener(this);
	}

	public void dispose() {
		MrcTrafficController.instance().removeMrcListener(this);
		super.dispose();
	}

	public synchronized void message(MrcMessage l) {  // receive a message and log it
		nextLine("cmd: \""+l.toString()+"\"\n", "");
	}
	public synchronized void reply(MrcReply l) {  // receive a reply message and log it
		nextLine("rep: \""+l.toString()+"\"\n", "");
	}

   static Logger log = Logger.getLogger(MrcMonFrame.class.getName());

}
