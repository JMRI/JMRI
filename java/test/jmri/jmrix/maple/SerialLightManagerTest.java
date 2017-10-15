package jmri.jmrix.maple;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SerialLightManagerTest {

    private MapleSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        memo = new MapleSystemConnectionMemo("K", "Maple");
        SerialLightManager lm = new SerialLightManager(memo);
        // create and register the light manager object
        Assert.assertNotNull("Maple Light Manager creation", lm);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SerialLightManagerTest.class);

}
