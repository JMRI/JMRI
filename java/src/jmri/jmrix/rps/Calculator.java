// Calculator.java
package jmri.jmrix.rps;

import javax.vecmath.Point3d;

/**
 * Describes a specific method of calculating a measurement
 * <P>
 * Information that the algorithm needs, e.g. received positions, etc, should be
 * provided to the actual object via ctor or other initialization. This
 * interface does not describe that.
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 * @version	$Revision$
 */
public interface Calculator {

    public Measurement convert(Reading r);

    public Measurement convert(Reading r, Point3d guess);

    public Measurement convert(Reading r, Measurement last);

}

/* @(#)Calculator.java */
