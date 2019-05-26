package jmri;

import jmri.DccLocoAddress;
import jmri.LocoAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This interface extends {@link jmri.IdTag} to include a locomotive address.
 * Typical uses are for RailCom and Transponding.  The default assumption is
 * that the tag ID is a locomotive address.
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
 * @author Paul Bender Copyright (C) 2019
 * @since 4.15.4
 */
public interface AddressedIdTag extends IdTag {

    /**
     * Gets the address reported back as a {@link jmri.LocoAddress}.
     *
     * @return current loco address
     */
    default public LocoAddress getLocoAddress() {
        int tagNo = Integer.parseInt(getTagID());
        return new DccLocoAddress(tagNo, tagNo > 100 );
    }
}
