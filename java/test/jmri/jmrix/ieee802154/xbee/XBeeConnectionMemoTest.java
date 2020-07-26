package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.XBeeDevice;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * XBeeConnectionMemoTest.java
 * <p>
 * Test for the jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
@ExtendWith(MockitoExtension.class)
public class XBeeConnectionMemoTest extends SystemConnectionMemoTestBase<XBeeConnectionMemo> {

    @Mock
    private XBeeTrafficController tc;
    @Mock
    private XBeeNetwork xn;
    private XBeeAdapter xa;
    private XBeeDevice xb;

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
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
        Mockito.when(tc.getXBee()).thenReturn(xb);
        scm.configureManagers();
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
