package jmri.jmrix.can.adapters.gridconnect.net;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class MergConnectionConfigTest extends jmri.jmrix.AbstractConnectionConfigTestBase {

    @Test
    public void testCTor() {
        MergConnectionConfig t = new MergConnectionConfig();
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new ConnectionConfig();
    }

    @After
    @Override
    public void tearDown() {
        cc = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MergConnectionConfigTest.class);

}
