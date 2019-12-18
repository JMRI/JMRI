package jmri.jmrix.can.cbus;

import jmri.CabSignal;
import jmri.implementation.DefaultCabSignal;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.LocoAddress;
import jmri.managers.AbstractCabSignalManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CBUS implementation of the {@link jmri.CabSignalManager} interface.
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
 * <p>
 *
 * @author Paul Bender Copyright (C) 2019
 * @author Steve Young Copyright (C) 2019 
 */
public class CbusCabSignalManager extends AbstractCabSignalManager {

    private CanSystemConnectionMemo _memo = null;

    public CbusCabSignalManager(CanSystemConnectionMemo memo){
        super();
        _memo = memo;
        log.debug("CBUS Cab Signal Manager initialized");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CabSignal createCabSignal(LocoAddress address){
           return new CbusCabSignal(_memo,address);
    }

    private final static Logger log = LoggerFactory.getLogger(CbusCabSignalManager.class);
}
