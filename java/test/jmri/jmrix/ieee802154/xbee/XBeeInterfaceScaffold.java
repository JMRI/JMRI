package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.RemoteXBeeDevice;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test scaffold implementation of XBeeInterface. Use an object of this type as
 * a XBeeTrafficController in tests.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2006
 * @author Paul Bender Copyright (C) 2016
 */
public class XBeeInterfaceScaffold extends XBeeTrafficController {

    private XBeeDevice localDevice;
    private XBeeAdapter a;
    public boolean dataSent = false;  // public OK for unit tests
    /**
     * Record of XBee messages sent, providing access for making sure they are
     * OK.
     */
    // public OK for unit tests
    public List<XBeeMessage> outbound = new ArrayList<>();

    public XBeeInterfaceScaffold() {
        super();
        // setup the mock XBee Connection.
        a = new XBeeAdapter() {
            @Override
            public boolean isOpen() {
                return true;
            }

            @Override
            public void close() {
            }
        };
        // Create the local device.
        localDevice = new XBeeDevice(a) {
            @Override
            public void sendData(RemoteXBeeDevice remoteXBeeDevice, byte[] data) {
                // set the data sent flag.
                dataSent = true;
            }

            @Override
            public void sendDataAsync(RemoteXBeeDevice remoteXBeeDevice, byte[] data) {
                // set the data sent flag.
                dataSent = true;
            }
        };

    }

    // override some XBeeTrafficController methods for test purposes
    @Override
    public boolean status() {
        return true;
    }

    @Override
    public XBeeDevice getXBee() {
        return localDevice;
    }

    @Override
    public void sendXBeeMessage(XBeeMessage m, XBeeListener replyTo) {
        if (log.isDebugEnabled()) {
            log.debug("sendXBeeMessage [{}]", m);
        }
        // save a copy
        outbound.add(m);
    }

    // test control member functions
    /**
     * forward a message to the listeners, e.g. test receipt
     *
     * @param m the message to forward
     */
    public void sendTestMessage(XBeeReply m) {
        // forward a test message to XBeeListeners
        log.debug("sendTestMessage    [{}]", m);
        notifyReply(m, null);
    }

    /*
     * Check number of listeners, used for testing dispose()
     */
    public int numListeners() {
        return cmdListeners.size();
    }

    /**
     * Avoid error message, normal in parent
     */
    @Override
    protected void connectionWarn() {
    }

    /**
     * Avoid error message, normal in parent
     *
     * @param e the error to ignore
     */
    @Override
    protected void portWarn(Exception e) {
    }

    @Override
    public void receiveLoop() {
    }

    @Override
    protected void terminate() {
        if (localDevice != null) {
            localDevice.close();
        }
        localDevice = null;
        a = null;
    }

    public void dispose() {
        terminate();
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeInterfaceScaffold.class);

}
