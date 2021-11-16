package jmri;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * PhysicalLocation
 *
 * Represents a physical location on the layout in 3D space.
 *
 * Dimension units are not specified, but should be kept consistent in all three
 * dimensions for a given usage.
 *
 * @author Mark Underwood    Copyright (C) 2011
 * @author Daniel Bergqvist  Copyright (C) 2021
 */
public interface PhysicalLocation {

    // Class methods
    /**
     * Origin : constant representation of (0, 0, 0)
     */
    public static final PhysicalLocation Origin = new OriginPhysicalLocation();

    /**
     * NBPropertyKey : Key name used when storing a PhysicalLocation as a
     * NamedBean Property
     */
    public static final String NBPropertyKey = "physical_location";

    /**
     * setBeanPhysicalLocation(PhysicalLocation p, NamedBean b)
     *
     * Store PhysicalLocation p as a property in NamedBean b.
     *
     * @param p PhysicalLocation
     * @param b NamedBean
     */
    public static void setBeanPhysicalLocation(PhysicalLocation p, NamedBean b) {
        b.setProperty(NBPropertyKey, p.toString());
    }

    public Vector3f getVector3f();
    
    public Vector3d toVector3d();

    public boolean isTunnel();

    public void setIsTunnel(boolean t);

    public float getX();

    public float getY();

    public float getZ();

    public void setX(float value);

    public void setY(float value);

    public void setZ(float value);



    static class OriginPhysicalLocation implements PhysicalLocation {

        private static final Vector3f origin = new Vector3f(0.0f, 0.0f, 0.0f);
        private static final Vector3d originAsVector3d = new Vector3d(origin);

        @Override
        public Vector3f getVector3f() {
            return origin;
        }

        @Override
        public Vector3d toVector3d() {
            return originAsVector3d;
        }

        @Override
        public boolean isTunnel() {
            return false;
        }

        @Override
        public void setIsTunnel(boolean t) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public float getX() {
            return origin.getX();
        }

        @Override
        public float getY() {
            return origin.getY();
        }

        @Override
        public float getZ() {
            return origin.getZ();
        }

        @Override
        public void setX(float value) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setY(float value) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setZ(float value) {
            throw new UnsupportedOperationException("Not supported");
        }

    }
    
}
