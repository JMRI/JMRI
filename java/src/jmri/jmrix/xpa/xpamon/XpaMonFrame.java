// XpaMonFrame.java

package jmri.jmrix.xpa.xpamon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.xpa.XpaListener;
import jmri.jmrix.xpa.XpaMessage;
import jmri.jmrix.xpa.XpaTrafficController;

/**
 * Frame displaying (and logging) Xpa+Modem command messages
 * @author			Paul Bender   Copyright (C) 2004
 * @version			$Revision$
 */
public class XpaMonFrame extends jmri.jmrix.AbstractMonFrame implements XpaListener {

	public XpaMonFrame() {
		super();
	}

	protected String title() { return "Xpa Command Monitor"; }

	protected void init() {
		// connect to TrafficController
		XpaTrafficController.instance().addXpaListener(this);
	}

	public void dispose() {
		XpaTrafficController.instance().removeXpaListener(this);
		super.dispose();
	}

	public synchronized void message(XpaMessage l) {  // receive a message and log it
		nextLine("Sent: \""+l.toString()+"\"\n", "");
	}
	public synchronized void reply(XpaMessage l) {  // receive a reply message and log it
		nextLine("Recieved: \""+l.toString()+"\"\n", "");
	}

   static Logger log = LoggerFactory.getLogger(XpaMonFrame.class.getName());

}
