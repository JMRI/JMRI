package jmri.jmrix.ecos;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class EcosDccThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",tm);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        EcosTrafficController tc = new EcosInterfaceScaffold();
        tm = new EcosDccThrottleManager(new jmri.jmrix.ecos.EcosSystemConnectionMemo(tc));
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EcosDccThrottleManagerTest.class);

}
