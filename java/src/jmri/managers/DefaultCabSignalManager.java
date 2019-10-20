package jmri.managers;

import java.util.HashMap;
import java.util.Set;
import jmri.CabSignal;
import jmri.CabSignalListListener;
import jmri.CabSignalManager;
import jmri.LocoAddress;
import jmri.implementation.DefaultCabSignal;

/**
 * Default implementation of the {@link jmri.CabSignalManager} interface.
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
 * @author Paul Bender Copyright (C) 2019
 */
public class DefaultCabSignalManager extends AbstractCabSignalManager {

    /**
     * {@inheritDoc}
     */
    @Override
    protected CabSignal createCabSignal(LocoAddress address){
           return new DefaultCabSignal(address);
    }

}
