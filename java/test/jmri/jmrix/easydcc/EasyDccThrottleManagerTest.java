package jmri.jmrix.easydcc;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EasyDccThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    private EasyDccTrafficControlScaffold tc = null;
    private EasyDccSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        // infrastructure objects
        Assert.assertNotNull("exists",tm);
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new EasyDccTrafficControlScaffold(null);
        memo = new EasyDccSystemConnectionMemo(tc);
        tm = new EasyDccThrottleManager(memo);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(EasyDccThrottleManagerTest.class);

}
