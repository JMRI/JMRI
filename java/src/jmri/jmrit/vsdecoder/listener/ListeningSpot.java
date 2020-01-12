package jmri.jmrit.vsdecoder.listener;

/**
 * class ListeningSpot
 *
 * Represents a defined spot for viewing (and therefore listening to) a layout.
 *
 */

/*
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author   Mark Underwood Copyright (C) 2012
 */
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import jmri.util.PhysicalLocation;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListeningSpot {

    private Vector3d _location;
    private Vector3d _up;
    private Vector3d _lookAt;
    private String _name;

    private static final Vector3d _atVector = new Vector3d(0.0d, 1.0d, 0.0d);
    private static final Vector3d _upVector = new Vector3d(0.0d, 0.0d, 1.0d);

    public ListeningSpot() {
        _name = null;
        _location = new Vector3d();
        _up = _upVector;
        _lookAt = _atVector;
    }

    public ListeningSpot(Vector3f position) {
        _name = null;
        _location = new Vector3d(position);
        _up = _upVector;
        _lookAt = _atVector;
    }

    public ListeningSpot(Vector3d position) {
        this(null, position);
    }

    public ListeningSpot(String name, Vector3d position) {
        _name = name;
        _location = position;
        _lookAt = _atVector;
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
        return (_name);
    }

    public Vector3d getLocation() {
        return (_location);
    }

    public PhysicalLocation getPhysicalLocation() {
        return (new PhysicalLocation(_location.x, _location.y, _location.z));
    }

    public Vector3d getUpVector() {
        return (_up);
    }

    public Vector3d getLookAtVector() {
        return (_lookAt);
    }

    /* TRig notes
     * Trig x = map y
     * Trig y = map x
     * bearing = theta
     * azimuth = 90 - rho
     * map y = r sin (90-azimuth) cos bearing
     * map x = r sin (90-azimuth) sin bearing
     * map z = r cos (90-azimuth)
     * r = sqrt( x^2 + y^2 + z^2 )
     * bearing = theta = atan(map x / map y)
     * azimuth = 90 - rho = 90 - acos(z / r)
     */
    public Double getBearing() {
        // bearing = theta = atan(map x / map y)
        Vector3d lav= getLookAtVector();
        Double b = Math.toDegrees(Math.atan(lav.x / lav.y));

        // lookAt point is behind listener
        if (lav.y < 0.0d) {
            b = b + 180.0d;
        } else if (b < 0.0d) {
            b = b + 360.0d;
        }
        return b;
    }

    public Double getAzimuth() {
        // r = sqrt( x^2 + y^2 + z^2 )
        // azimuth = 90 - rho = 90 - acos(z / r)
        Vector3d lav = getLookAtVector();
        Double r = Math.sqrt(lav.x * lav.x + lav.y * lav.y + lav.z * lav.z);
        return 90 - Math.toDegrees(Math.acos(lav.z / r));
    }

    public void setName(String n) {
        _name = n;
    }

    public void setLocation(Vector3d loc) {
        _location = loc;
    }

    public void setLocation(Double x, Double y, Double z) {
        if (x == null) {
            x = 0.0d;
        } else {
            x = checkLimits(x);
            x = roundDecimal(x);
        }
        if (y == null) {
            y = 0.0d;
        } else {
            y = checkLimits(y);
            y = roundDecimal(y);
        }
        if (z == null) {
            z = 0.0d;
        } else {
            z = checkLimits(z);
            z = roundDecimal(z);
        }
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
        up.cross(_la, _upVector);
        up.cross(up, _la);
        up.normalize();
        return (up);
    }

    public void setOrientation(Double bearing, Double azimuth) {
        // Convert bearing + azimuth to look-at and up vectors.
        // Bearing measured clockwise from Y axis.
        // Azimuth measured up (or down) from X/Y plane.
        // map y = r sin (90-azimuth) cos bearing
        // map x = r sin (90-azimuth) sin bearing
        // map z = r cos (90-azimuth)
        // Assumes r = 1;
        if (bearing == null) {
            bearing = 0.0d;
        }

        if (azimuth == null) {
            azimuth = 0.0d;
        }

        if (azimuth > 90.0d) {
            azimuth = 180.0d - azimuth;
        } else if (azimuth < -90.0d) {
           azimuth = -180.0d - azimuth;
        }

        double y = Math.sin(Math.toRadians(90 - azimuth)) * Math.cos(Math.toRadians(bearing)); 
        double x = Math.sin(Math.toRadians(90 - azimuth)) * Math.sin(Math.toRadians(bearing));
        double z = Math.cos(Math.toRadians(90 - azimuth));
        _lookAt = new Vector3d(x, y, z);
        _up = calcUpFromLookAt(_lookAt);
 
        _lookAt.x = roundDecimal(_lookAt.x);
        _lookAt.y = roundDecimal(_lookAt.y);
        _lookAt.z = roundDecimal(_lookAt.z);

        _up.x = roundDecimal(_up.x);
        _up.y = roundDecimal(_up.y);
        _up.z = roundDecimal(_up.z);
    }

    public Boolean equals(ListeningSpot other) {
        if ((this._name.equals(other.getName()))
                && (this._location == other.getLocation())
                && (this._up == other.getUpVector())
                && (this._lookAt == other.getLookAtVector())) {
            return (true);
        } else {
            return (false);
        }
    }

    private Vector3d parseVector3d(String pos) {
        if (pos == null) {
            return (null);
        }

        // position is stored as a tuple string "(x,y,z)"
        // Regex [-+]?[0-9]*\.?[0-9]+
        String syntax = "\\((\\s*[-+]?[0-9]*\\.?[0-9]+),(\\s*[-+]?[0-9]*\\.?[0-9]+),(\\s*[-+]?[0-9]*\\.?[0-9]+)\\)";
        try {
            Pattern p = Pattern.compile(syntax);
            Matcher m = p.matcher(pos);
            if (!m.matches()) {
                log.error("String does not match a valid position pattern. syntax= " + syntax + " string = " + pos);
                return (null);
            }
            // ++debug
            String xs = m.group(1);
            String ys = m.group(2);
            String zs = m.group(3);
            log.debug("Loading Vector3d: x = " + xs + " y = " + ys + " z = " + zs);
            // --debug
            return (new Vector3d(Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)), Double.parseDouble(m.group(3))));
        } catch (PatternSyntaxException e) {
            log.error("Malformed Vector3d syntax! " + syntax);
            return (null);
        } catch (IllegalStateException e) {
            log.error("Group called before match operation executed syntax=" + syntax + " string= " + pos + " " + e.toString());
            return (null);
        } catch (IndexOutOfBoundsException e) {
            log.error("Index out of bounds " + syntax + " string= " + pos + " " + e.toString());
            return (null);
        }
    }

    @Override
    public String toString() {
        if ((_location == null) || (_lookAt == null) || (_up == null)) {
            return ("ListeningSpot (undefined)");
        } else {
            return ("ListeningSpot Name: " + _name + " Location: " + _location.toString() + " LookAt: " + _lookAt.toString() + " Up: " + _up.toString());
        }
    }

    public Element getXml(String elementName) {
        Element me = new Element(elementName);
        me.setAttribute("name", (_name == null ? "default" : _name));
        me.setAttribute("location", _location.toString());
        me.setAttribute("up", _up.toString());
        me.setAttribute("look_at", _lookAt.toString());
        return (me);
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

    private static double roundDecimal(double value) {
        return (double) Math.round(value * 100) / 100;
    }

    private static final double MAX_DIST = 9999.99d;

    private static double checkLimits(double value) {
        value = Math.max(-MAX_DIST, value);
        value = Math.min(MAX_DIST, value);
        return value;
    }

    private static final Logger log = LoggerFactory.getLogger(ListeningSpot.class);

}
