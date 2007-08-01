// Region.java

package jmri.jmrix.rps;

import javax.vecmath.*;

/**
 * Represent a region in space for the RPS system.
 *<P>
 * The region is specfied by a <em>right-handed</em>
 * set of points.
 *
 * @author	Bob Jacobsen  Copyright (C) 2007
 * @version	$Revision: 1.1 $
 */
public class Region {
    
    public Region(Point3d[] points) {
        if (points.length<3) log.error("Not enough points to define region");
        this.points = points;
    }
    
    /**
     * Ctor from a string like "(0,0,0);(1,0,0);(1,1,0);(0,1,0)"
     */
    public Region(String s) {
        String[] pStrings = s.split(";");
        points = new Point3d[pStrings.length];
        
        // load each point
        for (int i=0; i<points.length; i++) {
            // remove leading ( and trailing )
            String coords = pStrings[i].substring(1,pStrings[i].length()-1);
            String[] coord = coords.split(",");
            if (coord.length!=3) log.error("need to have three coordinates in "+pStrings[i]);
            double x = Double.valueOf(coord[0]).doubleValue();
            double y = Double.valueOf(coord[1]).doubleValue();
            double z = Double.valueOf(coord[2]).doubleValue();
            points[i] = new Point3d(x,y,z);
        }
    }

    public boolean isInside(Point3d p) {
        for (int i = 0; i<points.length; i++) {
            int next = i+1;
            if (next >= points.length) next = 0;
            
            // check orientation of point offset and edge
            Vector3d edge = new Vector3d(
                                points[next].x-points[i].x,
                                points[next].y-points[i].y,
                                points[next].z-points[i].z);
                                
            Vector3d offset = new Vector3d(
                                p.x-points[i].x,
                                p.y-points[i].y,
                                p.z-points[i].z);
            
            offset.cross(offset,edge);               
            if (log.isDebugEnabled()) log.debug(""+i+" finds "+offset.z);
            if (offset.z > 0) return false;
        }

        // passed all        
        return true;
    }
    
    public boolean equals(Region r) {
        if (points.length != r.points.length) return false;
        for (int i = 0; i<points.length; i++)
            if (!points[i].epsilonEquals(r.points[i], 0.001)) return false;
        return true;
    }
    
    Point3d[] points;
    
    private final static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Region.class.getName());
}

/* @(#)Region.java */
