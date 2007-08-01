// Measurement.java

package jmri.jmrix.rps;

/**
 * Encodes a single measurement point for RPS
 * <P>
 * Immutable
 *
 * @author	Bob Jacobsen  Copyright (C) 2006
 * @version	$Revision: 1.1 $
 */
public class Measurement {

    public Measurement(Reading r) {
        this.r = r;
    }
        
    public Measurement(Reading r, double x, double y, double z, double vsound, int code, String source) {
        this(r);
        this.x = x;
        this.y = y;
        this.z = z;
        this.vsound = vsound;
        this.code = code;
        this.source = source;
    }
    
    /**
     * Return the Reading this 
     * measurement made from.
     *<P>
     * By definition, Reading objects are immutable
     *
     */
    public Reading getReading() {
        return r;
    }

    /**
     * Return the ID int of the transmitter
     * this measurement describes
     */
    public int getID() {
        if (r==null) return -1;
        return r.getID();
    }
    
    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }

    public double getZ(){
        return z;
    }
    
    public double getVSound(){
        return vsound;
    }
 
    /**
     * Error code, defined specifically by generator
     */
    public int getCode(){
        return code;
    }
    
    /**
     * Get name of the source
     */
    public String getSource(){
        return source;
    }
    
    double x,y,z, vsound;
    int code;
    String source;
    
    Reading r;  // a Reading object is by definition immutable

    public String toString() {
        String r = "Measurement id="+getID()+" position= "
                    +x+", "+y+", "+z;
        return r;
    }
}

/* @(#)Measurement.java */
