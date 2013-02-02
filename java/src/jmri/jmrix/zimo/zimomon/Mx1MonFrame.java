// Mx1MonFrame.java

package jmri.jmrix.zimo.zimomon;

import org.apache.log4j.Logger;
import jmri.jmrix.zimo.Mx1Listener;
import jmri.jmrix.zimo.Mx1TrafficController;
import jmri.jmrix.zimo.Mx1Message;

/**
 * Frame displaying (and logging) MX-1 messages
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @version         $Revision$
 *
 * Adapted by Sip Bosch for use with MX-1
 */

 public class Mx1MonFrame extends jmri.jmrix.AbstractMonFrame implements Mx1Listener {

	public Mx1MonFrame() {
		super();
	}

	protected String title() { return "MX-1 Traffic"; }

	public void dispose() {

		// disconnect from the LnTrafficController
		Mx1TrafficController.instance().removeMx1Listener(~0,this);

		// and unwind swing
		super.dispose();
	}

	protected void init() {
		// connect to the TrafficController
		Mx1TrafficController.instance().addMx1Listener(~0, this);
	}

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SBSC_USE_STRINGBUFFER_CONCATENATION", justification="string concatenation, efficiency not as important as clarity here")
	public synchronized void message(Mx1Message l) {  // receive a MX-1 message and log it
		// display the raw data if requested
		String raw = "packet: ";
		if ( rawCheckBox.isSelected() ) {
			int len = l.getNumDataElements();
			for (int i=0; i<len; i++)
				raw += Integer.toHexString(l.getElement(i))+" ";
		}

		// display the decoded data
		nextLine(l.toString()+"\n", raw);
	}

	static Logger log = Logger.getLogger(Mx1MonFrame.class.getName());

}

