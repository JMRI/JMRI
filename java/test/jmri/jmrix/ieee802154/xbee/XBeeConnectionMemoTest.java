package jmri.jmrix.ieee802154.xbee;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.XBeeDevice;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * XBeeConnectionMemoTest.java
 * <p>
 * Test for the jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class XBeeConnectionMemoTest extends SystemConnectionMemoTestBase<XBeeConnectionMemo> {

    private XBeeTrafficController tc;
    private XBeeNetwork xn;
    private XBeeAdapter xa;
    private XBeeDevice xb;

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assertions.assertFalse(scm.provides(jmri.ConsistManager.class), "Provides ConsistManager");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = mock(XBeeTrafficController.class);
        xn = mock(XBeeNetwork.class);
        scm = new XBeeConnectionMemo();
        scm.setTrafficController(tc);
        xa = new XBeeAdapter() {
            @Override
            public boolean isOpen() {
                return true;
            }
        };
        xb = new XBeeDevice(xa) {
            @Override
            public XBeeNetwork getNetwork() {
                return xn;
            }
        };
        when(tc.getXBee()).thenReturn(xb);
        scm.configureManagers();
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
