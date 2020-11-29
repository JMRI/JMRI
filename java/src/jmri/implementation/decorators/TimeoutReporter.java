package jmri.implementation.decorators;

import jmri.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Timeout decorator implementation for reporters.
 * <p>
 * This decorator causes the current report to be reset to nullified after a
 * preset time period.  This is to be used for reporter hardware that reports
 * a value, but never reports the value is cleared (e.g. most RFID readers).
 * <hr>
 * This file is part of JMRI.
 * <p>
 * based on TimeoutRfidReporter originally implemented by Matthew Harris
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * Based
 * @author Matthew Harris Copyright (C) 2014
 * @author Paul Bender Copyright (C) 2020
 * @since 4.19.4
 */
public class TimeoutReporter extends AbstractNamedBeanDecorator implements Reporter, IdTagListener,PropertyChangeListener {

    /**
     * The reporter this object is a decorator for
     */
    private Reporter reporter;


    /**
     * Timeout in ms
     */
    private static final int TIMEOUT = 2000;

    /**
     * Time when something was last reported by this object
     */
    private long whenLastReported = 0;

    /**
     * Reference to the timeout thread for this object
     */
    private TimeoutThread timeoutThread = null;

    public TimeoutReporter(Reporter reporter) {
        super(reporter);
        this.reporter = reporter;
        this.reporter.addPropertyChangeListener(this);
    }

    @Override
    public Object getLastReport() {
        return reporter.getLastReport();
    }

    @Override
    public Object getCurrentReport() {
        return reporter.getCurrentReport();
    }

    @Override
    public void setReport(Object r) {
        reporter.setReport(r);
    }

    @Override
    public int getState() {
        return reporter.getState();
    }

    @Override
    public void dispose() {
        super.dispose();
        reporter.dispose();
    }

    @Override
    public void setState(int i) throws JmriException {
        reporter.setState(i);
    }

    @Override
    public void notify(IdTag r) {
        if(reporter instanceof IdTagListener ){
            ((IdTagListener)reporter).notify(r);
        }
    }

    /**
     * {@inheritDoc}
     *
     *  Intercepts property change events from the underlying reporter
     *  and forwards them to property change listeners for this reporter.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        log.debug("event received {}",evt);
         if(evt.getPropertyName().equals("currentReport") &&
            evt.getNewValue()!=null ){
                 whenLastReported = System.currentTimeMillis();
                 if (timeoutThread == null) {
                     timeoutThread = new TimeoutThread();
                     timeoutThread.start();
                 }
         }
         // fire off the property change for listeners of this TimeoutReporter.
         firePropertyChange(evt.getPropertyName(),evt.getOldValue(),evt.getNewValue());
    }

    private void cleanUpTimeout() {
        log.debug("Cleanup timeout thread for {}",getSystemName());
        timeoutThread = null;
    }

    private class TimeoutThread extends Thread {

        TimeoutThread() {
            super();
            this.setName("Timeout-" + getSystemName());
        }

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            while ((whenLastReported + TIMEOUT) > System.currentTimeMillis()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }
            reporter.setReport(null);
            log.debug("Timeout-{}", getSystemName());
            cleanUpTimeout();
        }
    }

    private static final Logger log = LoggerFactory.getLogger(TimeoutReporter.class);

}
