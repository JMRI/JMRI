// TimeDisplay.java

package jmri;

/**
 * Provide access to basic functions of a clock face, that displays
 * time in some particular way.
 * <P>
 * Theres really not all that much here, and an abstract interface
 * is perhaps not yet needed.
 * <P>
 *
 * @author			Bob Jacobsen Copyright (C) 2004
 * @version			$Revision: 1.1 $
 */
public interface TimeDisplay {

    public void setUpdateRate(int msec);
    public int getUpdateRate();

}

/* @(#)TimeDisplay.java */
