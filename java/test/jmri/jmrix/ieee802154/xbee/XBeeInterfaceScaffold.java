package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.models.XBeeProtocol;
import java.util.Vector;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.mockito.mockpolicies.Slf4jMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@MockPolicy(Slf4jMockPolicy.class)

/**
 * XBeeInterfaceScaffold.java
 *
 * Description:	Test scaffold implementation of XBeeInterface
 *
 * @author	Bob Jacobsen Copyright (C) 2002, 2006
 * @author	Paul Bender Copyright (C) 2016
 *
 * Use an object of this type as a XBeeTrafficController in tests
 */
public class XBeeInterfaceScaffold extends XBeeTrafficController {

    private XBeeDevice localDevice;
    private RemoteXBeeDevice remoteDevice1;
    public XBeeInterfaceScaffold() {
        super();

        // setup the mock XBee Connection.
        // Mock the local device.
        XBeeAdapter a = PowerMockito.mock((XBeeAdapter.class));
        Mockito.when(a.isOpen()).thenReturn(true);
        localDevice = new XBeeDevice(a);

        // Mock the remote device 1.
        remoteDevice1 = Mockito.mock(RemoteXBeeDevice.class);
        Mockito.when(remoteDevice1.getXBeeProtocol()).thenReturn(XBeeProtocol.UNKNOWN);
        Mockito.when(remoteDevice1.getNodeID()).thenReturn("Node 1");
        Mockito.when(remoteDevice1.get64BitAddress()).thenReturn(new XBee64BitAddress("0013A20040A04D2D"));
        Mockito.when(remoteDevice1.get16BitAddress()).thenReturn(new XBee16BitAddress("0002"));

        byte pan[] = {(byte) 0x00, (byte) 0x42};
        byte uad[] = {(byte) 0x00, (byte) 0x02};
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        XBeeNode node = new XBeeNode(pan,uad,gad);
        node.setXBee(remoteDevice1);
        registerNode(node);

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

    // allow classes to get remoteDevice1
    public RemoteXBeeDevice getRemoteDevice1(){
       return remoteDevice1;
    }


    /**
     * record XBee messages sent, provide access for making sure they are OK
     */
    public Vector<XBeeMessage> outbound = new Vector<XBeeMessage>();  // public OK here, so long as this is a test class
 
    @Override
    public void sendXBeeMessage(XBeeMessage m, XBeeListener replyTo) {
        if (log.isDebugEnabled()) {
            log.debug("sendXBeeMessage [" + m + "]");
        }
        // save a copy
        outbound.addElement(m);
    }

    // test control member functions
    /**
     * forward a message to the listeners, e.g. test receipt
     */
    public void sendTestMessage(XBeeReply m) {
        // forward a test message to XBeeListeners
        if (log.isDebugEnabled()) {
            log.debug("sendTestMessage    [" + m + "]");
        }
        notifyReply(m, null);
        return;
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
     */
    @Override
    protected void portWarn(Exception e) {
    }

    @Override
    public void receiveLoop() {
    }

    public void dispose(){
          localDevice=null;
          remoteDevice1=null;
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeInterfaceScaffold.class);

}
