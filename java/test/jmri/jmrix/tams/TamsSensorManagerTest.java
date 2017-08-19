package jmri.jmrix.tams;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TamsSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    @Override
    public String getSystemName(int i){
       return "TMS" + i;
    }

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",l);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        TamsTrafficController tc = new TamsInterfaceScaffold();
        TamsSystemConnectionMemo memo = new TamsSystemConnectionMemo(tc);  
        l = new TamsSensorManager(memo);
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TamsSensorManagerTest.class.getName());

}
