package jmri.managers;

import jmri.JmriException;
import jmri.jmrix.SystemConnectionMemo;

/**
 * Default implementation for controlling layout power
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2010
 * @author Randall Wood Copyright 2020
 */
public class DefaultPowerManager extends AbstractPowerManager {

    public DefaultPowerManager(SystemConnectionMemo memo) {
        super(memo);
    }

    // to free resources when no longer used
    @Override
    public void dispose() throws JmriException {
        // do nothing
    }

}
