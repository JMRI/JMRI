// Reading.java
package jmri.jmrix.rps;


/**
 * Encodes a single set of input values (a "reading") for RPS.
 * <P>
 * The values are in time units (nominally usec), and need to be converted to
 * space units during later calculations.
 * <p>
 * The values are indexed by Receiver number, as known to the RPS system. For
 * example, getValue(2) will return the time from RPS receiver 2.
 *
 * <p>
 * Objects of this class are immutable once created.
 *
 * @author	Bob Jacobsen Copyright (C) 2006, 2008
 * @version	$Revision$
 */
@net.jcip.annotations.Immutable
public class Reading {

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI_EXPOSE_REP2") // We accept the external access by design
    public Reading(String id, double[] values) {
        this.id = id;
        this.values = values;
        this.rawData = null;
        this.time = 0;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI_EXPOSE_REP2") // We accept the external access by design
    public Reading(String id, double[] values, String raw) {
        this.id = id;
        this.values = values;
        this.rawData = raw;
        this.time = 0;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI_EXPOSE_REP2") // We accept the external access by design
    public Reading(String id, double[] values, int time) {
        this.id = id;
        this.values = values;
        this.rawData = null;
        this.time = time;
    }

    public Reading(Reading r) {
        this.id = r.getID();
        this.values = r.getValues();
        this.rawData = null;
        this.time = r.getTime();
    }

    /**
     * Return the time at which this Reading was requested
     */
    public int getTime() {
        return time;
    }

    /**
     * Return the ID int of the transmitter this reading describes
     */
    public String getID() {
        return id;
    }

    /**
     * NValues is really the highest receiver number possible.
     */
    public int getNValues() {
        return values.length - 1;
    }

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
        for (int i = 0; i < values.length; i++) {
            retval[i] = values[i];
        }
        return retval;
    }

    final String id;
    final double[] values;
    final int time; // in msec since epoch

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION") // We accept the poor performance
    public String toString() {
        String r = "Reading id=" + getID() + " values=";
        for (int i = 1; i <= getNValues(); i++) {
            r += "" + (int) getValue(i) + ((i != (getNValues())) ? "," : " ");
        }
        return r;
    }

    /**
     * Get the raw data from which this Reading was made.
     *
     * @return null if raw data was not preserved
     */
    public Object getRawData() {
        return rawData;
    }

    final Object rawData;
}

/* @(#)Reading.java */
