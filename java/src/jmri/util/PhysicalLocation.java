package jmri.util;

/*
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author			Mark Underwood Copyright (C) 2011
 * @version			$Revision: 18568 $
 */

import javax.vecmath.Vector3f;
import javax.vecmath.Vector3d;
import java.util.regex.*;
import jmri.NamedBean;

/** PhysicalLocation
 * 
 * Represents a physical location on the layout in 3D space.
 *
 * Dimension units are not specified, but should be kept consistent in
 * all three dimensions for a given usage.
 *
 * Used by VSDecoder for spatially positioning sounds on the layout.
 * 
 * Could also be used, for example, for velocity calculations between
 * sensors, or for keying operations locations or panel icons to a
 * physical map view of the layout.
 *
 */


public class PhysicalLocation extends Vector3f {

    float[] f = new float[3];  // used for extracting a single dimension
                               // from the underlying vector.

    private boolean _isTunnel;

    // Class methods

    /** Origin : constant representation of (0, 0, 0) */
    public static final PhysicalLocation Origin = new PhysicalLocation(0.0f, 0.0f, 0.0f);

    /** NBPropertyKey : Key name used when storing a PhysicalLocation as a NamedBean Property */
    public static final String NBPropertyKey = "physical_location";

    /** translate()
     *
     * Return a PhysicalLocation that represents the position of point "loc" relative to
     * reference point "ref".
     *
     * @param loc : PhysicalLocation to translate
     * @param ref : PhysicalLocation to use as new reference point (origin)
     * @return PhysicalLocation 
     */
    public static PhysicalLocation translate(PhysicalLocation loc, PhysicalLocation ref) {
	if (loc == null || ref == null)
	    return(loc);
	PhysicalLocation rv = new PhysicalLocation();
	rv.setX(loc.getX() - ref.getX());
	rv.setY(loc.getY() - ref.getY());
	rv.setZ(loc.getZ() - ref.getZ());
	return(rv);
    }

    /** getBeanPhysicalLocation(NamedBean b) 
     *
     * Extract the PhysicalLocation stored in NamedBean b, and return as
     * a new PhysicalLocation object.
     *
     * If the given NamedBean does not have a PhysicalLocation property
     * returns Origin.  (should probably return null instead, but...)
     *
     * @param b : NamedBean
     * @return PhysicalLocation
     */
    public static PhysicalLocation getBeanPhysicalLocation(NamedBean b) {
	String s = (String)b.getProperty(PhysicalLocation.NBPropertyKey);
	if ((s == null) || (s.equals(""))) {
	    return(PhysicalLocation.Origin);
	}
	else {
	    return(PhysicalLocation.parse(s));
	}
    }

    /** setBeanPhysicalLocation(PhysicalLocation p, NamedBean b) 
     * 
     * Store PhysicalLocation p as a property in NamedBean b.
     *
     * @param p PhysicalLocation
     * @param b NamedBean
     */
    public static void setBeanPhysicalLocation(PhysicalLocation p, NamedBean b) {
	b.setProperty(PhysicalLocation.NBPropertyKey,  p.toString());
    }

    /** Get a panel component that can be used to view and/or edit a location. */
    static public PhysicalLocationPanel getPanel(String title) {
	return(new PhysicalLocationPanel(title));
    }

    /** Parse a string representation (x,y,z)
     * Returns a new PhysicalLocation object.
     *
     * @param pos : String "(X, Y, Z)"
     * @return PhysicalLocation
     */
    static public PhysicalLocation parse(String pos) {
	// position is stored as a tuple string "(x,y,z)"
	// Optional flags come immediately after the (x,y,z) in the form of "(flag)".
	// Flags are boolean. If they are present, they are true.
	// Regex [-+]?[0-9]*\.?[0-9]+
	//String syntax = "\\((\\s*[-+]?[0-9]*\\.?[0-9]+),(\\s*[-+]?[0-9]*\\.?[0-9]+),(\\s*[-+]?[0-9]*\\.?[0-9]+)\\)";
	String syntax = "\\((\\s*[-+]?[0-9]*\\.?[0-9]+),(\\s*[-+]?[0-9]*\\.?[0-9]+),(\\s*[-+]?[0-9]*\\.?[0-9]+)\\)(\\([tunnel]\\))*";
	try {
	    Pattern p = Pattern.compile(syntax);
	    Matcher m = p.matcher(pos);
	    if (!m.matches()) { 
		log.error("String does not match a valid position pattern. syntax= " + syntax + " string = " + pos);
		return(null);
	    }
	    // ++debug
	    String xs = m.group(1);
	    String ys = m.group(2);
	    String zs = m.group(3);
	    log.debug("Loading position: x = " + xs + " y = " + ys + " z = " + zs);
	    // --debug
	    boolean is_tunnel = false;
	    // Handle optional flags
	    for (int i = 4; i < m.groupCount()+1; i++) {
		if ((m.group(i) != null) && (m.group(i) == "(tunnel)"))
		    is_tunnel = true;
	    }
	    
	    return(new PhysicalLocation(Float.parseFloat(m.group(1)), 
					Float.parseFloat(m.group(2)), 
					Float.parseFloat(m.group(3)),
					is_tunnel));
	} catch(PatternSyntaxException e) {
	    log.error("Malformed listener position syntax! " + syntax);
	    return(null);
	} catch(IllegalStateException e) {
	    log.error("Group called before match operation executed syntax=" + syntax + " string= " + pos + " " + e.toString());
	    return(null);
	} catch (IndexOutOfBoundsException e) {
	    log.error("Index out of bounds " + syntax + " string= " + pos + " " + e.toString());
	    return(null);
	}	
    }

    /** toString()
     *
     * Output a string representation (x,y,z)
     *
     * @return String  "(X, Y, Z)"
     */
    public String toString() {
	String s = "(" + this.getX() + ", "+ this.getY() + ", " + this.getZ() + ")";
	if (_isTunnel) {
	    s += "(tunnel)";
	}
	return(s);
    }

    public Vector3d toVector3d() {
	return(new Vector3d(this));
    }

    // Instance methods

    /** Default constructor */
    public PhysicalLocation() {
	super();
	_isTunnel = false;
    }

    /** Constructor from Vector3f */
    public PhysicalLocation(Vector3f v) {
	super(v);
	_isTunnel = false;
    }

    public PhysicalLocation(Vector3d v) {
	super(v);
	_isTunnel = false;
    }

    /** Constructor from X, Y, Z (float)  + is_tunnel (boolean) */
    public PhysicalLocation(float x, float y, float z, boolean is_tunnel) {
	super(x, y, z);
	_isTunnel = is_tunnel;

    }

    /** Constructor from X, Y, Z (float) */
    public PhysicalLocation(float x, float y, float z) {
	_isTunnel = false;

    }

    /** Constructor from X, Y, Z (double) */
    public PhysicalLocation(double x, double y, double z) {
	super((float)x, (float)y, (float)z);
	_isTunnel = false;
    }

    /** Constructor from X, Y, Z (double) */
    public PhysicalLocation(double x, double y, double z, boolean is_tunnel) {
	super((float)x, (float)y, (float)z);
	_isTunnel = is_tunnel;
    }

    /** Copy Constructor */
    public PhysicalLocation(PhysicalLocation p) {
	super(p.getX(), p.getY(), p.getZ());
	_isTunnel = p.isTunnel();
    }

    /** Get X dimension */
    public float getX() {
	this.get(f);
	return(f[0]);
    }

    /** Set X dimension */
    public void setX(float x) {
	this.get(f);
	f[0] = x;
	this.set(f);
    }

    /** Get Y dimension */
    public float getY() {
	this.get(f);
	return(f[1]);
    }

    /** Set Y dimension */
    public void setY(float y) {
	this.get(f);
	f[1] = y;
	this.set(f);
    }

    /** Get Z dimension */
    public float getZ() {
	this.get(f);
	return(f[2]);
    }

    /** Set Z dimension */
    public void setZ(float z) {
	this.get(f);
	f[2] = z;
	this.set(f);
    }

    public boolean isTunnel() {
	return(_isTunnel);
    }

    public void setIsTunnel(boolean t) {
	_isTunnel = t;
    }

    /** equals() */
    public Boolean equals(PhysicalLocation l) {
	if ((this.getX() == l.getX()) && 
	    (this.getY() == l.getY()) && 
	    (this.getZ() == l.getZ()) &&
	    (this.isTunnel() == l.isTunnel())) {
	    return (true);
	} else {
	    return(false);
	}
    }

    /** translate()
     *
     * Translate this PhysicalLocation's coordinates to be relative to point "ref".
     * NOTE: This is a "permanent" internal translation, and permanently changes
     * this PhysicalLocation's value.
     *
     * If you want a new PhysicalLocation that represents the relative position,
     * call the class method translate(loc, ref)
     *
     *  @param ref new reference (origin) point
     */
    public void translate(PhysicalLocation ref) {
	if (ref == null)
	    return;

	this.setX(this.getX() - ref.getX());
	this.setY(this.getY() - ref.getY());
	this.setZ(this.getZ() - ref.getZ());
    }



    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PhysicalLocation.class.getName());
}