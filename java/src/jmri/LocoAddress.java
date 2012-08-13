// LocoAddress.java

package jmri;


/** 
 * Inteface for generic Locomotive Address.
 *
 * Note that this is not DCC-specific.
 *
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
 * @author			Bob Jacobsen Copyright (C) 2005
 * @version			$Revision$
 */

public interface LocoAddress {

    public int getNumber();

    public int getProtocol();

    final static public int DCC = 0x01;
    final static public int DCC_SHORT = 0x02;
    final static public int DCC_LONG = 0x04;
    final static public int SELECTRIX = 0x06;
    final static public int MOTOROLA = 0x08;
    final static public int MFX = 0x10;

}


/* @(#)LocoAddress.java */
