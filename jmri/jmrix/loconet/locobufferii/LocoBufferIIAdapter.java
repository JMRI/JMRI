// LocoBufferAdapter.java

package jmri.jmrix.loconet.locobufferii;

import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;

/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it 
 * refers to the switch settings on the new LocoBuffer II
 
 * @author			Bob Jacobsen   Copyright (C) 2004
 * @version			$Revision: 1.2 $
 */
public class LocoBufferIIAdapter extends LocoBufferAdapter {


    public LocoBufferIIAdapter() {
        super();
        m2Instance = this;
    }

    /**
     * Get an array of valid baud rates. This is currently just a message
     * saying its fixed
     */
    public String[] validBaudRates() {
        return validSpeeds;
    }
    protected String [] validSpeeds = new String[]{"19,200 baud (Sw1 off, Sw3 off)", 
    											"57,600 baud (Sw1 on, Sw3 off)"};

    public String option1Name() { return "LocoBuffer-II connection uses "; }

    static public LocoBufferAdapter instance() {
        if (m2Instance == null) m2Instance = new LocoBufferIIAdapter();
        return m2Instance;
    }
    static LocoBufferIIAdapter m2Instance = null;

}
