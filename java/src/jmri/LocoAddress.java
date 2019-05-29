package jmri;

/**
 * Interface for generic Locomotive Address.
 *
 * Note that this is not DCC-specific.
 *
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Bob Jacobsen Copyright (C) 2005
 */
@javax.annotation.concurrent.Immutable
public interface LocoAddress {

    public int getNumber();

    public Protocol getProtocol();

    public enum Protocol {

        DCC_SHORT("dcc_short", "ProtocolDCC_Short"), // NOI18N
        DCC_LONG("dcc_long", "ProtocolDCC_Long"), // NOI18N
        DCC("dcc", "ProtocolDCC"), // NOI18N
        SELECTRIX("selectrix", "ProtocolSelectrix"), // NOI18N
        MOTOROLA("motorola", "ProtocolMotorola"), // NOI18N
        MFX("mfx", "ProtocolMFX"), // NOI18N
        M4("m4", "ProtocolM4"), // NOI18N
        OPENLCB("openlcb", "ProtocolOpenLCB"), // NOI18N
        LGB("lgb", "ProtocolLGB");   // NOI18N

        Protocol(String shName, String peopleKey) {
            this.shortName = shName;
            this.peopleName = Bundle.getMessage(peopleKey);
        }

        String shortName;
        String peopleName;

        public String getShortName() {
            return shortName;
        }

        public String getPeopleName() {
            return peopleName;
        }

        static public Protocol getByShortName(String shName) {
            for (Protocol p : Protocol.values()) {
                if (p.shortName.equals(shName)) {
                    return p;
                }
            }
            throw new java.lang.IllegalArgumentException("argument value " + shName + " not valid");
        }

        static public Protocol getByPeopleName(String pName) {
            for (Protocol p : Protocol.values()) {
                if (p.peopleName.equals(pName)) {
                    return p;
                }
            }
            throw new java.lang.IllegalArgumentException("argument value " + pName + " not valid");
        }

    }

}
