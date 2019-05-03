package jmri.jmrix.rps;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Encodes a single measurement point for RPS
 * <p>
 * Immutable
 *
 * @author	Bob Jacobsen Copyright (C) 2006
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
     * Return the Reading this measurement made from.
     * <p>
     * By definition, Reading objects are immutable
     *
     */
    public Reading getReading() {
        return r;
    }

    /**
     * Return the ID int of the transmitter this measurement describes
     */
    public String getId() {
        if (r == null) {
            return "<none>";
        }
        return r.getId();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getVSound() {
        return vsound;
    }

    boolean valid = true;

    public boolean isValidPosition() {
        if (!valid) {
            return false;
        }
        return !(Math.abs(x) > 1.E10 || Math.abs(x) > 1.E10 || Math.abs(x) > 1.E10);
    }

    public void setValidPosition(boolean val) {
        valid = val;
    }

    /**
     * Error code, defined specifically by generator
     */
    public int getCode() {
        return code;
    }

    /**
     * Should this be considered a valid measurement?
     */
    public boolean isOkPoint() {
        if (getCode() > 0) {
            return true;
        }
        return false;
    }

    /**
     * Get the error code as a human-readable string
     */
    public String textCode() {
        return "" + getCode();
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
    public String getSource() {
        return source;
    }

    double x, y, z, vsound;
    protected int code;
    String source;

    Reading r;  // a Reading object is by definition immutable

    @Override
    public String toString() {
        if (!isValidPosition()) {
            // out-of-range
            return "Measurement id=" + getId() + " invalid position";
        }
        return "Measurement id=" + getId() + " position= "
                + truncate(x) + ", " + truncate(y) + ", " + truncate(z);
    }

    // provide a quick decimal truncation for formatting
    double truncate(double x) {
        return (int) Math.round(x * 10) / 10.;
    }

}
