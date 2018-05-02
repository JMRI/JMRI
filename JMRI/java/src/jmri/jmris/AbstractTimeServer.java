package jmri.jmris;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Timebase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract interface between the JMRI (fast) clock and a network connection
 *
 * @author Paul Bender Copyright (C) 2013
 * @author Randall Wood Copyright (C) 2014
 */
abstract public class AbstractTimeServer {

    private static final Logger log = LoggerFactory.getLogger(AbstractTimeServer.class);
    protected PropertyChangeListener timeListener = null;
    protected Timebase timebase = null;

    public AbstractTimeServer() {
        this.timebase = InstanceManager.getDefault(jmri.Timebase.class);
    }

    /*
     * Protocol Specific Abstract Functions
     */
    abstract public void sendTime() throws IOException;

    abstract public void sendRate() throws IOException;

    abstract public void sendStatus() throws IOException;

    abstract public void sendErrorStatus() throws IOException;

    abstract public void parseTime(String statusString) throws JmriException, IOException;

    abstract public void parseRate(String statusString) throws JmriException, IOException;

    // wrapper around the clock control start and stop functions.
    public void startTime() {
        this.timebase.setRun(true);
    }

    public void stopTime() {
        this.timebase.setRun(false);
    }

    public void dispose() {
        if (this.timeListener != null) {
            this.timebase.removeMinuteChangeListener(timeListener);
            this.timeListener = null;
        }
        this.timebase = null;
    }

    public void listenToTimebase(boolean listen) {
        if (listen == false && timeListener == null) {
            return; // nothing to do.
        }
        if (timeListener == null) {
            timeListener = new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    try {
                        if (evt.getPropertyName().equals("minutes")) {
                            sendTime();
                        } else if (evt.getPropertyName().equals("run")) {
                            sendStatus();
                        } else {
                            sendRate();
                        }
                    } catch (IOException ex) {
                        log.warn("Unable to send message to client: {}", ex.getMessage());
                        timebase.removeMinuteChangeListener(timeListener);
                    }
                }
            };
        }
        if (listen == true) {
            timebase.addMinuteChangeListener(timeListener);
        } else {
            timebase.removeMinuteChangeListener(timeListener);
        }
    }
}
