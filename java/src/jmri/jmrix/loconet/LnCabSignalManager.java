package jmri.jmrix.loconet;

import jmri.managers.AbstractCabSignalManager;
import java.util.HashMap;
import jmri.LocoAddress;
import jmri.CabSignal;
import jmri.implementation.DefaultCabSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LocoNet implementation of the {@link jmri.CabSignalManager} interface.
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
public class LnCabSignalManager extends AbstractCabSignalManager {

    private LocoNetSystemConnectionMemo _memo = null;

    public LnCabSignalManager(LocoNetSystemConnectionMemo memo){
         super();
         _memo = memo;
         log.debug("LocoNet Cab Signal Manager initialized");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CabSignal createCabSignal(LocoAddress address){
           return new LnCabSignal(_memo,address);
    }

    private final static Logger log = LoggerFactory.getLogger(LnCabSignalManager.class);
}
