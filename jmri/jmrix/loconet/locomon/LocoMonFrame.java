// LocoMonFrame.java

package jmri.jmrix.loconet.locomon;

import jmri.jmrix.loconet.*;

/**
 * LocoNet Monitor Frame displaying (and logging) LocoNet messages
 * @author	   Bob Jacobsen   Copyright (C) 2001, 2008
 * @version   $Revision: 1.5 $
 */
public class LocoMonFrame extends jmri.jmrix.AbstractMonFrame implements LocoNetListener {

    public LocoMonFrame() {
        super();
    }

    protected String title() { return "LocoNet Traffic"; }

    public void dispose() {
        // disconnect from the LnTrafficController
        LnTrafficController.instance().removeLocoNetListener(~0,this);
        // and unwind swing
        super.dispose();
    }

    protected void init() {
        // connect to the LnTrafficController
        LnTrafficController.instance().addLocoNetListener(~0, this);
    }

    public synchronized void message(LocoNetMessage l) {  // receive a LocoNet message and log it
        // display the raw data if requested
        String raw = null ;
        if( rawCheckBox.isSelected() )
			raw = l.toString();

        // display the decoded data
        // we use Llnmon to format, expect it to provide consistent \n after each line
        nextLine(llnmon.displayMessage(l), raw );
    }

    jmri.jmrix.loconet.locomon.Llnmon llnmon = new jmri.jmrix.loconet.locomon.Llnmon();

    static char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' } ;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoMonFrame.class.getName());
}
