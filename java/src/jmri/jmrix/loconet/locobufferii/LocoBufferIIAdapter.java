// LocoBufferAdapter.java

package jmri.jmrix.loconet.locobufferii;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;

/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it 
 * refers to the switch settings on the new LocoBuffer II
 
 * @author			Bob Jacobsen   Copyright (C) 2004
 * @version			$Revision$
 */
public class LocoBufferIIAdapter extends LocoBufferAdapter {


    public LocoBufferIIAdapter() {
        super();
    }

    /**
     * Get an array of valid baud rates. This is modified to
     * have different comments.  Because the speeds are the same
     * as the parent class (19200 and 57600), we don't override
     * validBaudNumber().
     */
    public String[] validBaudRates() {
        return new String[]{"19,200 baud (Sw1 off, Sw3 off)", 
    						"57,600 baud (Sw1 on, Sw3 off)"};
    }

    public String option1Name() { return "LocoBuffer-II connection uses "; }
    
    static Logger log = LoggerFactory.getLogger(LocoBufferIIAdapter.class.getName());
}
