package jmri.jmrix.can.cbus.eventtable;

import jmri.implementation.AbstractShutDownTask;

/**
 * Class to call dispose on a MERG CBUS event table.
 * This saves the event data to XML prior to shutdown.
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusEventTableShutdownTask extends AbstractShutDownTask {

    private final CbusEventTableDataModel _model;
    
    /**
     * Constructor specifies the warning message and action to take
     *
     * @param name the name of the task (used in logs)
     * @param model the CBUS Event Table instance to dispose of
    */
    public CbusEventTableShutdownTask(String name, CbusEventTableDataModel model) {
        super(name);
        _model = model;
    }

    /**
    * Checks preferences, saving Table contents if necessary.
    */
    @Override
    public void run() {
        _model.dispose();
    }
}
