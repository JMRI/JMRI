package jmri.jmrix.rfid;

import jmri.implementation.AbstractIdTagReporter;

/**
 * Extend AbstractIdTagReporter for RFID systems
 * <p>
 * System names are "FRpppp", where ppp is a representation of the RFID reader.
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
 * @author Matthew Harris Copyright (c) 2011
 * @since 2.11.4
 */
public class RfidReporter extends AbstractIdTagReporter {

    public RfidReporter(String systemName) {
        super(systemName);
    }

    public RfidReporter(String systemName, String userName) {
        super(systemName, userName);
    }


}
