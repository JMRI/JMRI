package jmri.jmrix.secsi;

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

    private SerialTrafficControlScaffold tcis = null;
    private SecsiSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        SerialLightManager t = new SerialLightManager(memo);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new SerialTrafficControlScaffold();
        memo = new SecsiSystemConnectionMemo();
        memo.setTrafficController(tcis);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SerialLightManagerTest.class);

}
