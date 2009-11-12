// SignalMast.java

package jmri;

/**
 * Represent a signal mast.  A signal mast is one or more signal heads
 * that are treated as a single signal.  (imagine several heads 
 * attached to a single mast)
 * A signal may have more than one of these 
 * (e.g. a signal mast consisting of several heads)
 * when needed to represent more complex aspects, e.g. Diverging Appoach
 * etc.
 * <P>
 * A mast presents an Aspect, as that's a composite of the appearance
 * of several heads.
 * <P>
 * This class has one bound parameter:
 *<DL>
 *<DT>aspect<DD>The specific color being shown. Values are the
 * various color constants defined in the class. 
 * <p>
 * The aspect constants are integers to which the user may assign an
 * aspect name.
 *</dl>
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
 * @author			Bob Jacobsen Copyright (C) 2002, 2008
 * @author			Pete Cressman Copyright (C) 2009
 * @version			$Revision: 1.1 $
 */
public interface SignalMast extends NamedBean {

    /* Speed aspects as defined by Doughlas A. Kerr - "Rail Signal Aspects and Indications"
    * doug.kerr.home.att.net/pumpkin/Rail_Signal_Aspects.pdf 
    */
    public static final int SPEED_MASK      = 0x07;     // least significant 3 bits
    public static final int STOP_SPEED      = 0x01;     // No Speed
    public static final int RESTRICTED_SPEED= 0x02;     // Train able to stop within 1/2 visual range (10-15mph)
    public static final int SLOW_SPEED      = 0x03;     // Typically 15 mph  (25% of NORMAL)
    public static final int MEDIUM_SPEED    = 0x04;     // Typically 30 mph (50% of NORMAL)
    public static final int LIMITED_SPEED   = 0x05;     // Typically 40-45 mph  (75% of NORMAL)
    public static final int NORMAL_SPEED    = 0x06;     // Varies with road and location
    public static final int MAXIMUM_SPEED   = 0x07;     // "full" throttle

    /*
    * Aspects defined with the upper bits 5 to 16. (Reserve bit 4 for possible speed expansion)
    */

    /**
     * Aspect is a bound parameter. 
     */
    public int getAspect();
    public void setAspect(int newAspect);
    public String getAspectName();
    public String getAspectName(int aspect);
    
    public int[] getValidStates();
    public String[] getValidStateNames();
    
}


/* @(#)SignalMast.java */

