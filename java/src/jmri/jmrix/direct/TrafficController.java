package jmri.jmrix.direct;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import jmri.jmrix.AbstractSerialPortController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from NMRA packets and controls sending to the
 * direct interface.
 * <p>
 * This is much simpler than many other "TrafficHandler" classes, because
 *   <ul>
 *   <li>It's not handling mode information, or even any information back from the
 *   device; it's just sending.
 *   <li>It can work with the direct packets.
 *   </ul>
 * This actually bears more similarity to a pure implementation of the
 * CommandStation interface, which is where the real guts of it is. In
 * particular, note that transmission is not a threaded operation.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class TrafficController implements jmri.CommandStation {

    /**
     * Create a new Direct TrafficController instance.
     * @param memo system connection.
     */
    public TrafficController(DirectSystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    /**
     * Send a specific packet to the rails.
     *
     * @param packet  Byte array representing the packet, including the
     *                error-correction byte. Must not be null.
     * @param repeats Number of times to repeat the transmission, but is ignored
     *                in the current implementation
     */
    @Override
    public boolean sendPacket(byte[] packet, int repeats) {

        if (repeats != 1) {
            log.warn("Only single transmissions currently available");
        }

        // convert packet (in byte form) into bits
        int[] msgAsInt = MakePacket.createStream(packet);

        if (msgAsInt[0] == 0) {
            // failed to make packet
            log.error("Failed to convert packet to transmitable form: {}", java.util.Arrays.toString(packet));
            return false;
        }

        // have to recopy & reformat, as there's only a byte write in Java 1
        // note that msgAsInt has 0th byte containing length still
        byte[] msg = new byte[msgAsInt[0]];
        for (int i = 0; i < msg.length; i++) {
            msg[i] = (byte) (msgAsInt[i + 1] & 0xFF);
        }

        // and stream the resulting byte array
        try {
            if (ostream != null) {
                if (log.isDebugEnabled()) {
                    StringBuilder f = new StringBuilder();
                    for (int i = 0; i < msg.length; i++) {
                        f.append(Integer.toHexString(0xFF & msg[i])).append(" ");
                    }
                    log.debug("write message: {}", f);
                }
                ostream.write(msg);
            } else {
                // no stream connected
                log.warn("sendMessage: no connection established");
            }
        } catch (IOException e) {
            log.warn("sendMessage: Exception: {}", e.getMessage());
        }
        return true;
    }

    // methods to connect/disconnect to a source of data in an AbstractSerialPortController
    private DirectSystemConnectionMemo _memo = null;
    private AbstractSerialPortController controller = null;

    public boolean status() {
        return (ostream != null && istream != null);
    }

    /**
     * Make connection to existing PortController object.
     *
     * @param p the controller to connect to
     */
    public void connectPort(AbstractSerialPortController p) {
        istream = p.getInputStream();
        ostream = p.getOutputStream();
        if (controller != null) {
            log.warn("connectPort: connect called while connected");
        } else {
            log.debug("connectPort invoked");
        }
        controller = p;
    }

    /**
     * Break connection to existing PortController object. Once broken, attempts
     * to send via "message" member will fail.
     *
     * @param p the controller to disconnect from
     */
    public void disconnectPort(AbstractSerialPortController p) {
        istream = null;
        ostream = null;
        if (controller != p) {
            log.warn("disconnectPort: disconnect called from non-connected AbstractPortController");
        }
        controller = null;
    }

    // data members to hold the streams
    protected DataInputStream istream = null;
    protected OutputStream ostream = null;

    @Override
    public String getUserName() {
        return _memo.getUserName();
    }

    @Override
    public String getSystemPrefix() {
        return _memo.getSystemPrefix();
    }

    private final static Logger log = LoggerFactory.getLogger(TrafficController.class);

}
