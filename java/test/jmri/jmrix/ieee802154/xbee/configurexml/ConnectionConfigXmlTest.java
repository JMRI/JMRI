package jmri.jmrix.ieee802154.xbee.configurexml;

import com.digi.xbee.api.XBeeDevice;
import jmri.jmrix.ieee802154.xbee.XBeeNode;
import jmri.jmrix.ieee802154.xbee.XBeeTrafficController;
import jmri.util.JUnitUtil;

import jmri.util.ThreadingUtil;
import org.jdom2.Element;
import org.junit.Assume;
import org.junit.jupiter.api.*;

import jmri.jmrix.ieee802154.xbee.ConnectionConfig;
import org.mockito.Mockito;

import javax.swing.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

/**
 * ConnectionConfigXmlTest.java
 *
 * Test for the ConnectionConfigXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractSerialConnectionConfigXmlTestBase {


    @Test
    public void testLoadWithNode(){
        Assume.assumeNotNull(cc);
        // reset the profile manager for this test, so it can run independently.
        jmri.util.JUnitUtil.resetProfileManager();
        // This test requires a configure manager.
        jmri.util.JUnitUtil.initConfigureManager();
        // Running this on the UI thread fixes some ConcurrentModificationExceptions errors.
        ThreadingUtil.runOnGUI(()->{
            cc.loadDetails(new JPanel());
            cc.setDisabled(true); // so we don't try to start the connection on load.

            XBeeDevice localDevice = Mockito.mock(XBeeDevice.class);

            XBeeTrafficController tc = Mockito.mock(XBeeTrafficController.class);
            Mockito.when(tc.getXBee()).thenReturn(localDevice);
            ((jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo) cc.getAdapter().getSystemConnectionMemo())
                    .setTrafficController(new XBeeTrafficController());

            byte pan[] = {(byte) 0x00, (byte) 0x42};
            byte uad[] = {(byte) 0x6D, (byte) 0x97};
            byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
            XBeeNode node = new XBeeNode(pan,uad,gad);
            node.setIdentifier("foo");
            ((jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo) cc.getAdapter().getSystemConnectionMemo())
                    .getTrafficController().registerNode(node);
        });
        // load details MAY produce an error message if no ports are found.
        jmri.util.JUnitAppender.suppressErrorMessage("No usable ports returned");
        Element e = xmlAdapter.store(cc);
        //load what we just produced.
        Throwable thrown = catchThrowable( () -> xmlAdapter.load(e, e));
        assertThat(thrown).isNull();
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new ConnectionConfigXml();
        cc = new ConnectionConfig();
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }
}
