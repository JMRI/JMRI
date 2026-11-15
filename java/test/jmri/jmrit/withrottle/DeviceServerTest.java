package jmri.jmrit.withrottle;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import jmri.InstanceManager;
import jmri.NamedBeanHandleManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of DeviceServer
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class DeviceServerTest {

    @Test
    public void testCtor() {
        java.net.Socket s = new java.net.Socket();
        FacelessServer f = new FacelessServer(){
           @Override
           public void listen(){
           }
        };
        DeviceServer panel = new DeviceServer(s,f);
        jmri.util.JUnitAppender.assertErrorMessage("Stream creation failed (DeviceServer)");
        Assert.assertNotNull("exists", panel );
    }


    /**
     * A device that stops reading must not hold up the layout thread: the
     * messages wait in a queue, and the device is disconnected when it falls
     * too far behind. The timeout makes a failure end the test rather than
     * hang it.
     *
     * @throws java.io.IOException if the sockets can't be opened
     */
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    public void testDeviceNotReadingDoesNotHoldLayoutThread() throws java.io.IOException {
        FacelessServer f = new FacelessServer(){
           @Override
           public void listen(){
           }
        };
        InetAddress loopback = InetAddress.getLoopbackAddress();
        try (ServerSocket listener = new ServerSocket(0, 1, loopback);
                Socket device = new Socket()) {
            device.setReceiveBufferSize(8192); // small buffers, so they fill quickly
            device.connect(new InetSocketAddress(loopback, listener.getLocalPort()));
            try (Socket socket = listener.accept()) {
                socket.setSendBufferSize(8192);
                DeviceServer server = new DeviceServer(socket, f);
                String packet = "x".repeat(1000);

                // the device reads nothing: 8 MiB is twice the queue limit
                ThreadingUtil.runOnLayout(() -> {
                    for (int i = 0; i < 8192; i++) {
                        server.sendPacketToDevice(packet);
                    }
                });

                JUnitUtil.waitFor(socket::isClosed, "socket closed when the device fell behind");
                JUnitAppender.assertWarnMessageStartsWith("Client WiThrottle ");
            }
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugCommandStation();
        InstanceManager.setDefault(NamedBeanHandleManager.class, new NamedBeanHandleManager());
    }
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }
}
