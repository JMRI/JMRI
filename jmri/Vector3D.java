// Vector3D.java

package jmri;

/**
 * This class represents a right-hand cartesian coordinate system and provides
 * some convenience vector arithmetic methods.
 *
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
 * @author Matthew Harris  copyright (c) 2009
 * @version $Revision: 1.1 $
 */
public class Vector3D {

    /**
     * x-coordinate of the vector
     * <pre>
     * (left-to-right)
     * (-ve   0   +ve)
     * </pre>
     */
    public float x;

    /**
     * y-coordinate of the vector
     * <pre>
     * (bottom-to-top)
     * (-ve   0   +ve)
     * </pre>
     */
    public float y;

    /**
     * z-coordinate of the vector
     * <pre>
     * (back-to-front)
     * (-ve   0   +ve)
     * </pre>
     */
    public float z;

    /**
     * Create new Vector3D at origin.
     */
    public Vector3D() {
        this.x = 0.0f;
        this.y = 0.0f;
        this.z = 0.0f;
    }

    /**
     * Create new Vector3D at specified point.
     * 
     * @param x x-coordinate (left-to-right)
     * @param y y-coordinate (bottom-to-top)
     * @param z z-coordinate (back-to-front)
     */
    public Vector3D(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Override the default class clone method to provide a new Vector3D
     * with the same coordinates as this object.
     *
     * @return new Vector3D
     */
    @Override
    public Vector3D clone() {
        return new Vector3D(x,y,z);
    }

    /**
     * Returns a string that represents the coordinates of this Vector3D
     * in the form [x, y, z].
     * 
     * @return String representation
     */
    @Override
    public String toString(){
        return "[" + this.x + ", "
                   + this.y + ", "
                   + this.z + "]";
    }

    /**
     * Static method to return a new Vector3D containing the cross product
     * of two Vector3D objects.
     * <pre>
     *
     * Where [a] = [a1, a2, a3] and [b] = [b1, b2, b3];
     * 
     * [a] × [b] = [a2b3 - a3b2, a3b1 - a1b3, a1b2 - a2b1].
     * </pre>
     * @param vecA first Vector3D object in the cross product
     * @param vecB second Vector3D object in the cross product
     * @return Vector3D containing resulting cross product
     */
    public static Vector3D cross(Vector3D vecA, Vector3D vecB) {
        return new Vector3D((vecA.y * vecB.z) - (vecA.z * vecB.y),
                            (vecA.z * vecB.x) - (vecA.x * vecB.z),
                            (vecA.x * vecB.y) - (vecA.y * vecB.x));
    }

    /**
     * Return a new Vector3D containing the cross product of this
     * Vector3D object to a second Vector3D object.
     * <pre>
     *
     * Where [a] = [a1, a2, a3] and [b] = [b1, b2, b3];
     * 
     * [a] × [b] = [a2b3 - a3b2, a3b1 - a1b3, a1b2 - a2b1].
     * </pre>
     * @param vecB second Vector3D object in the cross product
     * @return Vector3D containing resulting cross product
     */
    public Vector3D cross(Vector3D vecB) {
        return cross(this, vecB);
    }

    /**
     * Static method to return the dot product of two Vector3D objects.
     * <pre>
     *
     * Where [a] = [a1, a2, a3] and [b] = [b1, b2, b3];
     *
     * [a] · [b] = a1b1 + a2b2 + a3b3
     * </pre>
     * @param vecA first Vector3D in the dot product
     * @param vecB second Vector3D in the dot product
     * @return float containing the resulting dot product
     */
    public static float dot(Vector3D vecA, Vector3D vecB) {
        return((vecA.x * vecB.x)+
               (vecA.y * vecB.y)+
               (vecA.z * vecB.z));
    }

    /**
     * Return the dot product of this Vector3D object to a second
     * Vector3D object.
     * <pre>
     *
     * Where [a] = [a1, a2, a3] and [b] = [b1, b2, b3];
     * 
     * [a] · [b] = a1b1 + a2b2 + a3b3
     * </pre>
     * @param vecB second Vector3D object in the dot product
     * @return float containing the resulting dot product
     */
    public float dot(Vector3D vecB) {
        return dot(this, vecB);
    }

    /**
     * Static method to return the addition of two Vector3D objects.
     * <pre>
     *
     * Where [a] = [a1, a2, a3] and [b] = [b1, b2, b3];
     * 
     * [a] + [b] = [a1 + b1, a2 + b2, a3 + b3].
     * </pre>
     * @param vecA first Vector3D object in the addition
     * @param vecB second Vector3D object in the addition
     * @return Vector3D containing the resulting addition
     */
    
    public static Vector3D addition(Vector3D vecA, Vector3D vecB) {
        return new Vector3D((vecA.x + vecB.x),
                            (vecA.y + vecB.y),
                            (vecA.z + vecB.z));

    }

    /**
     * Return the addition of this Vector3D object to a second
     * Vector3D object.
     * <pre>
     *
     * Where [a] = [a1, a2, a3] and [b] = [b1, b2, b3];
     * 
     * [a] + [b] = [a1 + b1, a2 + b2, a3 + b3].
     * </pre>
     * @param vecB second Vector3D object in the addition
     * @return Vector3D containing the resulting addition
     */
    public Vector3D addition(Vector3D vecB) {
        return addition(this, vecB);
    }

    /**
     * Static method to return the subtraction of two Vector3D objects.
     * <pre>
     *
     * Where [a] = [a1, a2, a3] and [b] = [b1, b2, b3];
     * 
     * [a] - [b] = [a1 - b1, a2 - b2, a3 - b3].
     * </pre>
     * @param vecA first Vector3D object in the subtraction
     * @param vecB second Vector3D object in the subtraction
     * @return Vector3D containing the resulting subtraction
     */

    public static Vector3D subtraction(Vector3D vecA, Vector3D vecB) {
        return new Vector3D((vecA.x - vecB.x),
                            (vecA.y - vecB.y),
                            (vecA.z - vecB.z));

    }

    /**
     * Return the subtraction from this Vector3D object of a second
     * Vector3D object.
     * <pre>
     *
     * Where [a] = [a1, a2, a3] and [b] = [b1, b2, b3];
     *
     * [a] - [b] = [a1 - b1, a2 - b2, a3 - b3].
     * </pre>
     * @param vecB second Vector3D object in the subtraction
     * @return Vector3D containing the resulting subtraction
     */
    public Vector3D subtraction(Vector3D vecB) {
        return subtraction(this, vecB);
    }

    /**
     * If non-zero, set the length of this vector to 1.0f
     */
    public void normalise() {
        float t = (float) Math.sqrt((this.x * this.x)+
                                    (this.y * this.y)+
                                    (this.z * this.z));
        if (t!=0) {
            this.x = this.x / t;
            this.y = this.y / t;
            this.z = this.z / t;
        }
    }
}

/* $(#)Vector3D.java */