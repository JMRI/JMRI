package jmri.jmrix.rfid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import jmri.jmrix.AbstractStreamPortController;
import jmri.jmrix.rfid.generic.standalone.StandaloneReporterManager;
import jmri.jmrix.rfid.generic.standalone.StandaloneSensorManager;
import jmri.jmrix.rfid.generic.standalone.StandaloneSystemConnectionMemo;
import jmri.jmrix.rfid.generic.standalone.StandaloneTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base for classes representing a RFID communications port
 * <p>
 * NOTE: This currently only supports the standalone RFID interfaces.
 * <p>
 *
 * @author	Paul Bender Copyright (C) 2014
 */
public class RfidStreamPortController extends AbstractStreamPortController implements RfidInterface {

    public RfidStreamPortController(DataInputStream in, DataOutputStream out, String pname) {
        super(new StandaloneSystemConnectionMemo(), in, out, pname);
    }

    @Override
    public void configure() {
        log.debug("configure() called.");
        RfidTrafficController control = new StandaloneTrafficController(this.getSystemConnectionMemo());

        // connect to the traffic controller
        this.getSystemConnectionMemo().setRfidTrafficController(control);
        control.setAdapterMemo(this.getSystemConnectionMemo());
        this.getSystemConnectionMemo().configureManagers(
                new StandaloneSensorManager(control, this.getSystemPrefix()),
                new StandaloneReporterManager(control, this.getSystemPrefix()));
        control.connectPort(this);

        // declare up
        ActiveFlag.setActive();

    }

    /**
     * Check that this object is ready to operate. This is a question of
     * configuration, not transient hardware status.
     *
     * @return true
     */
    @Override
    public boolean status() {
        return true;
    }

    /**
     * Can the port accept additional characters?
     *
     * @return true
     */
    public boolean okToSend() {
        return (true);
    }

    // RFID Interface methods.
    @Override
    public void addRfidListener(RfidListener l) {
        this.getSystemConnectionMemo().getTrafficController().addRfidListener(l);
    }

    @Override
    public void removeRfidListener(RfidListener l) {
        this.getSystemConnectionMemo().getTrafficController().removeRfidListener(l);
    }

    @Override
    public void sendRfidMessage(RfidMessage m, RfidListener l) {
        this.getSystemConnectionMemo().getTrafficController().sendRfidMessage(m, l);
    }

    @Override
    public RfidSystemConnectionMemo getSystemConnectionMemo() {
        return (RfidSystemConnectionMemo) super.getSystemConnectionMemo();
    }

    private final static Logger log = LoggerFactory.getLogger(RfidStreamPortController.class.getName());

}
