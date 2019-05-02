package jmri.jmrix.rps;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.util.Arrays;
import javax.vecmath.Point3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represent a region in space for the RPS system.
 * <p>
 * The region is specified by a <em>right-handed</em>
 * set of points.
 * <p>
 * Regions are immutable once created.
 * <p>
 * This initial implementation of a Region is inherently 2-dimensional,
 * deferring use of the 3rd (Z) dimension to a later implementation. It uses a
 * Java2D GeneralPath to handle the inside/outside calculations.
 *
 * @author	Bob Jacobsen Copyright (C) 2007, 2008
 */
@javax.annotation.concurrent.Immutable
public class Region {

    public Region(Point3d[] points) {
        super();

        initPath(points);

        // old init
        if (points.length < 3) {
            log.error("Not enough points to define region");
        }
        this.points = Arrays.copyOf(points, points.length);
    }

    @SuppressFBWarnings(value = "JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS", justification = "internal state, not changeable from outside")
    GeneralPath path;

    /**
     * Ctor from a string like "(0,0,0);(1,0,0);(1,1,0);(0,1,0)"
     */
    public Region(String s) {
        String[] pStrings = s.split(";");
        points = new Point3d[pStrings.length];

        // load each point
        for (int i = 0; i < points.length; i++) {
            // remove leading ( and trailing )
            String coords = pStrings[i].substring(1, pStrings[i].length() - 1);
            String[] coord = coords.split(",");
            if (coord.length != 3) {
                log.error("need to have three coordinates in {}", pStrings[i]);
            }
            double x = Double.valueOf(coord[0]);
            double y = Double.valueOf(coord[1]);
            double z = Double.valueOf(coord[2]);
            points[i] = new Point3d(x, y, z);
        }
        initPath(points);
    }

    /**
     * Provide Java2D access to the shape of this region.
     * <p>
     * This should provide a copy of the GeneralPath path, to keep the
     * underlying object immutable, but by returning a Shape type hopefully we
     * achieve the same result with a little better performance. Please don't
     * assume you can cast and modify this.
     */
    public Shape getPath() {
        return path;
    }

    void initPath(Point3d[] points) {
        if (points.length < 3) {
            log.error("Region needs at least three points to have non-zero area");
        }

        path = new GeneralPath();
        path.moveTo((float) points[0].x, (float) points[0].y);
        for (int i = 1; i < points.length; i++) {
            path.lineTo((float) points[i].x, (float) points[i].y);
        }
        path.lineTo((float) points[0].x, (float) points[0].y);
    }

    @Override
    public String toString() {
        StringBuilder retval = new StringBuilder("");
        for (int i = 0; i < points.length; i++) {
            retval.append(String.format("(%f,%f,%f)", points[i].x, points[i].y, points[i].z));
            if (i != points.length - 1) {
                retval.append(";");
            }
        }
        return retval.toString();
    }

    public boolean isInside(Point3d p) {
        return path.contains(p.x, p.y);
    }

    @Override
    public boolean equals(Object ro) {
        if (ro == null || !(ro instanceof Region)) {
            return false;
        }
        try {
            Region r = (Region) ro;
            if (points.length != r.points.length) {
                return false;
            }
            for (int i = 0; i < points.length; i++) {
                if (!points[i].epsilonEquals(r.points[i], 0.001)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int code = 0;
        if (points.length >= 1) {
            code = 10000 * (int) points[0].x + 10000 * (int) points[0].y;
        }
        if (points.length >= 2) {
            code = code + 10000 * (int) points[1].x + 10000 * (int) points[1].y;
        }
        return code;
    }

    final Point3d[] points;

    private final static Logger log = LoggerFactory.getLogger(Region.class);

}
