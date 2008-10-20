// Reading.java

package jmri.jmrix.rps;

/**
 * Encodes a single set of input values (a "reading") for RPS.
 * <P>
 * The values are in time units (nominally usec), and need to be converted
 * to space units during later calculations.
 * <p>
 * The values are indexed by Receiver number, as known to the RPS system.
 * For example, getValue(2) will return the time from 
 * RPS receiver 2.
 *
 *<p>
 * Objects of this class are immutable once created.
 *
 * @author	Bob Jacobsen  Copyright (C) 2006, 2008
 * @version	$Revision: 1.6 $
 */
public class Reading {

    public Reading(int id, double[] values) {
        this.id = id;
        this.values = values;
    }
        
    public Reading(int id, double[] values, String raw) {
        this.id = id;
        this.values = values;
        this.rawData = raw;
    }
        
    public Reading(int id, double[] values, int time) {
        this.id = id;
        this.values = values;
        this.time = time;
    }
        
    public Reading(Reading r) {
        this.id = r.getID();
        this.values = r.getValues();
    }
    
    /**
     * Return the time at which this Reading was requested
     */
    public int getTime() {
        return time;
    }
    
    /**
     * Return the ID int of the transmitter
     * this reading describes
     */
    public int getID() {
        return id;
    }
    
    /**
     * NValues is
     * really the highest receiver number possible.
     */
    public int getNValues() { return values.length-1; }
    
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
    int time; // in msec since epoch
        
    public String toString() {
        String r = "Reading id="+getID()+" values=";
        for (int i = 1; i<=getNValues(); i++) 
            r+=""+(int)getValue(i)+((i!=(getNValues()))?",":" ");
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
