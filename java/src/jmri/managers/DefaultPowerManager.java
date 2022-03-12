package jmri.managers;

import jmri.JmriException;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

/**
 * Default implementation for controlling layout power
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2010
 * @author Randall Wood Copyright 2020
 */
public class DefaultPowerManager extends AbstractPowerManager<InternalSystemConnectionMemo> {

    public DefaultPowerManager(InternalSystemConnectionMemo memo) {
        super(memo);
    }

    // to free resources when no longer used
    @Override
    public void dispose() throws JmriException {
        // do nothing
    }

}
