// LocoBufferAdapter.java

package jmri.jmrix.loconet.locobufferii;

import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;

/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it 
 * refers to the switch settings on the new LocoBuffer II
 
 * @author			Bob Jacobsen   Copyright (C) 2004
 * @version			$Revision: 1.3 $
 */
public class LocoBufferIIAdapter extends LocoBufferAdapter {


    public LocoBufferIIAdapter() {
        super();
        m2Instance = this;
    }

    /**
     * Get an array of valid baud rates. This is modified to
     * have different comments.  Because the speeds are the same
     * as the parent class (19200 and 57600), we don't override
     * validBaudNumber().
     */
    public String[] validBaudRates() {
        return validSpeeds;
    }
    protected String [] validSpeeds = new String[]{"19,200 baud (Sw1 off, Sw3 off)", 
    											"57,600 baud (Sw1 on, Sw3 off)"};

    public String option1Name() { return "LocoBuffer-II connection uses "; }

    static public LocoBufferAdapter instance() {
        if (m2Instance == null) {
        	m2Instance = new LocoBufferIIAdapter();
        	log.debug("new default instance in LocoBufferIIAdapter");
        }
        log.debug("LocoBufferIIAdapter.instance returns object of class "+m2Instance.getClass().getName());
        return m2Instance;
    }
    static private LocoBufferIIAdapter m2Instance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoBufferIIAdapter.class.getName());
}
