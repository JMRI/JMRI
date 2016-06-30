package jmri.jmrix.sprog;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import jmri.jmrix.AbstractStreamPortController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base for classes representing an SPROG Command Station
 * communications port
 * <p>
 * NOTE: This currently only supports the SPROG Command Station interfaces.
 * <p>
 *
 * @author	Paul Bender Copyright (C) 2014
 */
public class SprogCSStreamPortController extends AbstractStreamPortController implements SprogInterface {

    private Thread rcvNotice = null;

    public SprogCSStreamPortController(DataInputStream in, DataOutputStream out, String pname) {
        super(new SprogSystemConnectionMemo(SprogConstants.SprogMode.OPS), in, out, pname);
    }

    @Override
    public void configure() {
        log.debug("configure() called.");
        SprogTrafficController control = new SprogTrafficController();

        // connect to the traffic controller
        this.getSystemConnectionMemo().setSprogTrafficController(control);
        control.setAdapterMemo(this.getSystemConnectionMemo());
        this.getSystemConnectionMemo().configureCommandStation();
        this.getSystemConnectionMemo().configureManagers();
        control.connectPort(this);

        // start thread to notify controller when data is available
        rcvNotice = new Thread(new rcvCheck(input, control));
        rcvNotice.start();

        // declare up
        ActiveFlag.setActive();

    }

    /**
     * Check that this object is ready to operate. This is a question of
     * configuration, not transient hardware status.
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

    // SPROG Interface methods.
    @Override
    public void addSprogListener(SprogListener l) {
        this.getSystemConnectionMemo().getSprogTrafficController().addSprogListener(l);
    }

    @Override
    public void removeSprogListener(SprogListener l) {
        this.getSystemConnectionMemo().getSprogTrafficController().removeSprogListener(l);
    }

    @Override
    public void sendSprogMessage(SprogMessage m, SprogListener l) {
        this.getSystemConnectionMemo().getSprogTrafficController().sendSprogMessage(m, l);
    }

    @Override
    public SprogSystemConnectionMemo getSystemConnectionMemo() {
        return (SprogSystemConnectionMemo) super.getSystemConnectionMemo();
    }

    // internal thread to check to see if the stream has data and
    // notify the Traffic Controller.
    static protected class rcvCheck implements Runnable {

        private SprogTrafficController control;
        private DataInputStream in;

        public rcvCheck(DataInputStream in, SprogTrafficController control) {
            this.in = in;
            this.control = control;
        }

        public void run() {
            do {
                try {
                    if (in.available() > 0) {
                        control.handleOneIncomingReply();
                    }
                } catch (java.io.IOException ioe) {
                    log.error("Error reading data from stream");
                }
                // need to sleep here?
            } while (true);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SprogCSStreamPortController.class.getName());

}
