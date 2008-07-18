// Measurement.java

package jmri.jmrix.rps;

import javax.vecmath.Vector3d;
import javax.vecmath.Point3d;

/**
 * Encodes a single measurement point for RPS
 * <P>
 * Immutable
 *
 * @author	Bob Jacobsen  Copyright (C) 2006
 * @version	$Revision: 1.3 $
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
    
    boolean valid = true;
    public boolean isValidPosition() { 
        if (!valid) return false;
        return !(Math.abs(x) > 1.E10 || Math.abs(x) > 1.E10 || Math.abs(x) > 1.E10);   
    }
    
    public void setValidPosition(boolean val) { valid = val; }
 
    /**
     * Error code, defined specifically by generator
     */
    public int getCode(){
        return code;
    }
    
    public Point3d getPoint() {
        return new Point3d(x, y, z);
    }
    
    public Vector3d getVector() {
        return new Vector3d(x, y, z);
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
        if (!isValidPosition()) {
            // out-of-range
            return "Measurement id="+getID()+" invalid position";
        }
        return "Measurement id="+getID()+" position= "
                    +truncate(x)+", "+truncate(y)+", "+truncate(z);
    }
    
    // provide a quick decimal truncation for formatting
    double truncate(double x) {
        return (double)((int)Math.round(x*10)/10.);
    }
}

/* @(#)Measurement.java */
