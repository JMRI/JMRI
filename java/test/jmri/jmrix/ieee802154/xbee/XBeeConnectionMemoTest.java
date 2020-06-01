package jmri.jmrix.ieee802154.xbee;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.XBeeDevice;
import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

/**
 * XBeeConnectionMemoTest.java
 * <p>
 * Test for the jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
@RunWith(MockitoJUnitRunner.class)
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

    @Before
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

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
