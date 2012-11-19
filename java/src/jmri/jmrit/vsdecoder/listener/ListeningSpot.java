package jmri.jmrit.vsdecoder.listener;

/** class ListeningSpot
 *
 * Represents a defined spot for viewing
 * (and therefore listening to) a layout.
 *
 */

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
 * @author			Mark Underwood Copyright (C) 2012
 * @version			$Revision: 21510 $
 */

import net.java.games.sound3d.Vec3f;
import javax.vecmath.Vector3d;
import jmri.util.PhysicalLocation;
import org.jdom.Element;
import java.util.regex.*;

public class ListeningSpot {

    private Vector3d _location;
    private Vector3d _up;
    private Vector3d _lookAt;
    private String _name;
    
    private static final Vector3d _rightVector = new Vector3d(0.0d, 1.0d, 0.0d);
    private static final Vector3d _upVector = new Vector3d(0.0d, 0.0d, 1.0d);

    public ListeningSpot() {
	_name = null;
	_location = new Vector3d();
	_up = new Vector3d(0.0d, 0.0d, 1.0d);
	_lookAt = new Vector3d(0.0d, 1.0d, 0.0d);
    }

    public ListeningSpot(String name, Vector3d position) {
	_name = name;
	_location = position;
	_lookAt = _rightVector;
	_up = _upVector;
    }

    public ListeningSpot(String name, Vector3d loc, Vector3d up, Vector3d at) {
	_name = name;
	_location = loc;
	_up = up;
	_lookAt = at;
    }

    public ListeningSpot(Element e) {
	this.setXml(e);
    }

    public String getName() {
	return(_name);
    }

    public Vector3d getLocation() {
	return(_location);
    }

    public PhysicalLocation getPhysicalLocation() {
	return(new PhysicalLocation(_location.x, _location.y, _location.z));
    }

    public Vector3d getUpVector() {
	return(_up);
    }

    public Vector3d getLookAtVector() {
	return(_lookAt);
    }

    public Double getBearing() {
	return(Math.toDegrees(Math.asin(_location.y / _location.x)));
    }

    public Double getAzimuth() {
	Double hypo = Math.sqrt(Math.pow(_location.x, 2.0d) + Math.pow(_location.y, 2.0d));
	return(Math.toDegrees(Math.asin(_location.z / hypo)));
    }

    public void setName(String n) {
	_name = n;
    }

    public void setLocation(Vector3d loc) {
	_location = loc;
    }

    public void setLocation(Double x, Double y, Double z) {
	_location = new Vector3d(x, y, z);
    }

    public void setLocation(PhysicalLocation l) {
	_location = new Vector3d(l.getX(), l.getY(), l.getZ());
    }

    public void setUpVector(Vector3d up) {
	_up = up;
    }

    public void setLookAtVector(Vector3d at) {
	_lookAt = at;
    }

    public void setOrientation(PhysicalLocation target) {
	Vector3d la = new Vector3d();
	// Calculate the look-at vector
	la.sub(target.toVector3d(), _location);  // la = target - location
	la.normalize();
	_lookAt = la;
	// Calculate the up vector
	_up = calcUpFromLookAt(la);
    }

    private Vector3d calcUpFromLookAt(Vector3d la) {
	Vector3d _la = la;
	_la.normalize();
	Vector3d up = new Vector3d();
	up.cross(_la, _rightVector);
	up.cross(up, _la);
	return(up);
    }

    public void setOrientation(double bearing, double azimuth) {
	// Convert bearing + azimuth to look-at and up vectors.
	// X = sin(baearing), Y = cos(bearing), Z = sin(azimuth)
	double theta_b = Math.toRadians(bearing);  // Bearing in radians
	double theta_a = Math.toRadians(azimuth);  // Azimuth in radians
	Double hypo = Math.sqrt(Math.pow(_location.x, 2.0d) + Math.pow(_location.y, 2.0d));
	_lookAt = new Vector3d(Math.sin(theta_b), Math.cos(theta_b), Math.sin(theta_a)*hypo);
	_up = calcUpFromLookAt(_lookAt);
    }

    public Boolean equals(ListeningSpot other) {
	if ((this._name.equals(other.getName())) &&
	    (this._location == other.getLocation()) &&
	    (this._up == other.getUpVector()) &&
	    (this._lookAt == other.getLookAtVector())) {
	    return(true);
	} else {
	    return(false);
	}
    }

    private Vector3d parseVector3d(String pos) {
	if (pos == null)
	    return(null);

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
	    log.debug("Loading Vector3d: x = " + xs + " y = " + ys + " z = " + zs);
	    // --debug
	    return(new Vector3d(Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)), Double.parseDouble(m.group(3))));
	} catch(PatternSyntaxException e) {
	    log.error("Malformed Vector3d syntax! " + syntax);
	    return(null);
	} catch(IllegalStateException e) {
	    log.error("Group called before match operation executed syntax=" + syntax + " string= " + pos + " " + e.toString());
	    return(null);
	} catch (IndexOutOfBoundsException e) {
	    log.error("Index out of bounds " + syntax + " string= " + pos + " " + e.toString());
	    return(null);
	}	
    }

    public String toString() {
	return(new String("ListeningSpot Name: " + _name + " Location: " + _location.toString() + " LookAt: " + _lookAt.toString() + " Up: " + _up.toString()));
    }
    
    public Element getXml(String elementName) {
	Element me = new Element(elementName);
	me.setAttribute("name", _name);
	me.setAttribute("location", _location.toString());
	me.setAttribute("up", _up.toString());
	me.setAttribute("look_at", _lookAt.toString());
	return(me);
    }

    public void setXml(Element e) {
	if (e != null) {
	    log.debug("ListeningSpot: " + e.getAttributeValue("name"));
	    _name = e.getAttributeValue("name");
	    _location = parseVector3d(e.getAttributeValue("location"));
	    _up = parseVector3d(e.getAttributeValue("up"));
	    _lookAt = parseVector3d(e.getAttributeValue("look_at"));
	}
    }
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ListeningSpot.class.getName());

}