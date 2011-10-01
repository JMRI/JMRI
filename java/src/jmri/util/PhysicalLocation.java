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
import java.util.regex.*;

/* PhysicalLocation
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

    public PhysicalLocation() {
	super();
    }

    public PhysicalLocation(Vector3f v) {
	super(v);
    }

    public PhysicalLocation(float x, float y, float z) {
	super(x, y, z);
    }

    public float getX() {
	this.get(f);
	return(f[0]);
    }

    public void setX(float x) {
	this.get(f);
	f[0] = x;
	this.set(f);
    }

    public float getY() {
	this.get(f);
	return(f[1]);
    }

    public void setY(float y) {
	this.get(f);
	f[1] = y;
	this.set(f);
    }

    public float getZ() {
	this.get(f);
	return(f[2]);
    }

    public void setZ(float z) {
	this.get(f);
	f[2] = z;
	this.set(f);
    }

    // Get a panel component that can be used to view and/or edit a location.
    static public PhysicalLocationPanel getPanel(String title) {
	return(new PhysicalLocationPanel(title));
    }

    // Parse a string representation (x,y,z)
    static public PhysicalLocation parse(String pos) {
	// position is stored as a tuple string "(x,y,z)"
	// Regex [-+]?[0-9]*\.?[0-9]+
	String syntax = "\\((\\s*[-+]?[0-9]*\\.?[0-9]+),(\\s*[-+]?[0-9]*\\.?[0-9]+),(\\s*[-+]?[0-9]*\\.?[0-9]+)\\)";
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
	    return(new PhysicalLocation(Float.parseFloat(m.group(1)), Float.parseFloat(m.group(2)), Float.parseFloat(m.group(3))));
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

    // Output a string representation (x,y,z)
    public String toString() {
	String s = "(" + this.getX() + ", "+ this.getY() + ", " + this.getZ() + ")";
	return(s);
    }


    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PhysicalLocation.class.getName());
}