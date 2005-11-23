// LocoBufferUsbAdapter.java

package jmri.jmrix.loconet.locobufferusb;

import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;

/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it 
 * refers to the switch settings on the new LocoBuffer-USB
 
 * @author			Bob Jacobsen   Copyright (C) 2004, 2005
 * @version			$Revision: 1.1 $
 */
public class LocoBufferUsbAdapter extends LocoBufferAdapter {


    public LocoBufferUsbAdapter() {
        super();
        m2Instance = this;
    }

    /**
     * Get an array of valid baud rates. 
     */
    public String[] validBaudRates() {
        return validSpeeds;
    }
    protected String [] validSpeeds = new String[]{"57,600 baud"};
    /**
     * Get an array of valid baud rates as integers. This allows subclasses
     * to change the arrays of speeds.
     */
    public int[] validBaudNumber() {
        return validSpeedValues;
    }
    protected int [] validSpeedValues = new int[]{57600};

    /**
     * Since option 1 is not used for this, return an array with one empty element
     */
    public String[] validOption1() { return new String[]{""}; }

    /**
     * Option 1 not used, so return a null string.
     */
    public String option1Name() { return ""; }


    static public boolean hasInstance() { return (null!=m2Instance); }
    static public LocoBufferAdapter instance() {
        if (m2Instance == null) {
        	m2Instance = new LocoBufferUsbAdapter();
        	log.debug("new default instance in LocoBufferUsbAdapter");
        }
        log.debug("LocoBufferUsbAdapter.instance returns object of class "+m2Instance.getClass().getName());
        return m2Instance;
    }
    static private LocoBufferUsbAdapter m2Instance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoBufferUsbAdapter.class.getName());
}
