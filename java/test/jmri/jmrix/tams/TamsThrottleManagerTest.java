package jmri.jmrix.tams;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TamsThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase{

    private TamsTrafficController tc;
    private TamsSystemConnectionMemo memo;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",tm);
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new TamsInterfaceScaffold();
        memo = new TamsSystemConnectionMemo(tc);
        tm = new TamsThrottleManager(memo);
    }

    @AfterEach
    public void tearDown() {
        tm.dispose();
        tm = null;
        memo.dispose();
        memo = null;
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TamsThrottleManagerTest.class);

}
