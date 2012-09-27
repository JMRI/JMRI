// LocoAddress.java

package jmri;

import java.util.ResourceBundle;

/** 
 * Interface for generic Locomotive Address.
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

    public Protocol getProtocol();

    public enum Protocol {
        DCC_SHORT(  "dcc_short","ProtocolDCC_Short"),
        DCC_LONG(   "dcc_long", "ProtocolDCC_Long"),
        DCC(        "dcc",      "ProtocolDCC"),
        SELECTRIX(  "selectrix","ProtocolSelectrix"),
        MOTOROLA(   "motorola", "ProtocolMotorola"),
        MFX(        "mfx",      "ProtocolMFX"),
        M4(         "m4",       "ProtocolM4"),
        OPENLCB(    "openlcb",  "ProtocolOpenLCB");
        
        static ResourceBundle rb;
        private static ResourceBundle getRB() { // needed due to order of initialization
            if (rb == null) rb = ResourceBundle.getBundle("jmri.ProtocolBundle");
            return rb;
        }

        Protocol(String shName, String peopleKey) {
            this.shortName = shName;
            this.peopleName = getRB().getString(peopleKey);
        }
        
        String shortName;
        String peopleName;
        
        public String getShortName() { return shortName; }
        public String getPeopleName() { return peopleName; }
        
        static public Protocol getByShortName(String shName) {
            for (Protocol p : Protocol.values()) {
                if (p.shortName.equals(shName)) return p;
            }
            throw new java.lang.IllegalArgumentException("argument value "+shName+" not valid");
        }
        
        static public Protocol getByPeopleName(String pName) {
            for (Protocol p : Protocol.values()) {
                if (p.peopleName.equals(pName)) return p;
            }
            throw new java.lang.IllegalArgumentException("argument value "+pName+" not valid");
        }
        
    }
    
}


/* @(#)LocoAddress.java */
