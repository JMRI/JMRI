// Reading.java

package jmri.jmrix.rps;

/**
 * Encodes a single set of input values (a "reading") for RPS.
 * <P>
 * The values are in time units (nominally usec), and need to be converted
 * to space units during later calculations.
 *
 * Objects of this class are immutable once created.
 *
 * @author	Bob Jacobsen  Copyright (C) 2006
 * @version	$Revision: 1.1 $
 */
public class Reading {

    public Reading(int id, double[] values) {
        this.id = id;
        this.values = values;
    }
        
    public Reading(Reading r) {
        this.id = r.getID();
        this.values = r.getValues();
    }
    
    /**
     * Return the ID int of the transmitter
     * this reading describes
     */
    public int getID() {
        return id;
    }
    
    public int getNSample() { return values.length; }
    
    /**
     * Convenience method to get a specific one of the values
     */
    public double getValue(int i) {
        return values[i];
    }

    /*
     * Get the entire data array as an copy,
     * to preserve immutability
     */
    public double[] getValues() {
        double[] retval = new double[values.length];
        for (int i=0; i<values.length; i++) retval[i] = values[i];
        return retval;
    }
        
    int id;
    double[] values;
        
    /**
     * Preserve a reference to raw data for possible later logging
     */
    void setRawData(Object o) {
        rawData = o;
    }
    
    public String toString() {
        String r = "Reading id="+getID()+" values=";
        for (int i = 0; i<getNSample(); i++) 
            r+=""+getValue(i)+((i!=(getNSample()-1))?",":" ");
        return r;
    }
    
    /**
     * Get the raw data from which this Reading was made.
     * @return null if raw data was not preserved
     */
    public Object getRawData() {return rawData;}
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Reading.class.getName());

    Object rawData;
}

/* @(#)Reading.java */
