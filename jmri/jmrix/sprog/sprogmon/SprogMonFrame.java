// SprogMonFrame.java

package jmri.jmrix.sprog.sprogmon;

import jmri.jmrix.sprog.SprogListener;
import jmri.jmrix.sprog.SprogMessage;
import jmri.jmrix.sprog.SprogReply;
import jmri.jmrix.sprog.SprogTrafficController;

/**
 * Frame displaying (and logging) Sprog command messages
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */
public class SprogMonFrame extends jmri.jmrix.AbstractMonFrame implements SprogListener {

	public SprogMonFrame() {
		super();
	}

	protected String title() { return "Sprog Command Monitor"; }

	protected void init() {
		// connect to TrafficController
		SprogTrafficController.instance().addSprogListener(this);
	}

	public void dispose() {
		SprogTrafficController.instance().removeSprogListener(this);
	}

	public synchronized void message(SprogMessage l) {  // receive a message and log it
		nextLine("cmd: \""+l.toString()+"\"\n", "");
	}
	public synchronized void reply(SprogReply l) {  // receive a reply message and log it
		nextLine("rep: \""+l.toString()+"\"\n", "");
	}

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogMonFrame.class.getName());

}
