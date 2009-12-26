// SignalMast.java

package jmri;

/**
 * Represent a signal mast.  A signal mast is one or more signal heads
 * that are treated as a single signal.  (Imagine several heads 
 * attached to a single mast, though other implementations are possible)
 * <P>
 * A mast presents an Aspect, as that's a composite of the appearance
 * of the entire signal.
 * <P>
 * This class has one bound parameter:
 *<DL>
 *<DT>aspect<DD>The specific aspect being shown.
 * <p>
 * Aspects are named by a user defined String name.
 *</dl>
 * The integer state (getState(), setState()) is the index of the
 * current aspect in the list of all defined aspects.
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
 * @version			$Revision: 1.3 $
 */
public interface SignalMast extends NamedBean {

    public void setAspect(String aspect);
    public String getAspect();
    
}


/* @(#)SignalMast.java */

