package jmri.jmrix.ieee802154.xbee;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.mockpolicies.Slf4jMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.modules.junit4.PowerMockRunner;
@MockPolicy(Slf4jMockPolicy.class)

/**
 * XBeeConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo
 * class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
@RunWith(PowerMockRunner.class)
public class XBeeConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @Test
    @Ignore("PowerMockRunner treats assumption failure as an error")
    public void getPowerManager(){
    }

    @Override
    @Test
    @Ignore("PowerMockRunner treats assumption failure as an error")
    public void getThrottleManager(){
    }

    @Override
    @Test
    @Ignore("PowerMockRunner treats assumption failure as an error")
    public void getReporterManager(){
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        XBeeConnectionMemo memo = new XBeeConnectionMemo();
        memo.setTrafficController(new XBeeInterfaceScaffold());
        memo.configureManagers();
        scm = memo;
    }

    @After
    public void tearDown() {
    }

}
