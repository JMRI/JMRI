package jmri.jmris;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract interface between the JMRI power manager and a network connection
 *
 * @author Paul Bender Copyright (C) 2010
 */
public abstract class AbstractPowerServer implements PropertyChangeListener {

    private InstanceManager instanceManager;

    public AbstractPowerServer(){
        this(InstanceManager.getDefault());
    }

    public AbstractPowerServer(InstanceManager instanceManager){

        this.instanceManager = instanceManager;
    }

    protected boolean mgrOK() {
        if (p == null) {
            p = instanceManager.getNullableDefault(PowerManager.class);
            if (p == null) {
                log.error("No power manager instance found");
                try {
                    sendErrorStatus();
                } catch (IOException ie) {
                }
                return false;
            } else {
                p.addPropertyChangeListener(this);
            }
        }
        return true;
    }

    public void setOnStatus() {
        if (mgrOK()) {
            try {
                p.setPower(PowerManager.ON);
            } catch (JmriException e) {
                log.error("Exception trying to turn power on " + e);
                try {
                    sendErrorStatus();
                } catch (IOException ie) {
                }
            }
        }
    }

    public void setOffStatus() {
        if (mgrOK()) {
            try {
                p.setPower(PowerManager.OFF);
            } catch (JmriException e) {
                log.error("Exception trying to turn power off " + e);
                try {
                    sendErrorStatus();
                } catch (IOException ie) {
                }
            }
        }
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent ev) {
        try {
            sendStatus(p.getPower());
        } catch (JmriException ex) {
            try {
                sendErrorStatus();
            } catch (IOException ie) {
            }
        } catch (IOException ie2) {
        }
    }

    public void dispose() {
        if (p != null) {
            p.removePropertyChangeListener(this);
        }
    }

    protected PowerManager p = null;

    /*
     * Protocol Specific Abstract Functions
     */
    abstract public void sendStatus(int Status) throws IOException;

    abstract public void sendErrorStatus() throws IOException;

    abstract public void parseStatus(String statusString) throws JmriException, IOException;

    private final static Logger log = LoggerFactory.getLogger(AbstractPowerServer.class);

}
