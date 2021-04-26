package jmri.jmrix.easydcc;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EasyDccProgrammerManagerTest {


    private EasyDccTrafficControlScaffold tc = null;
    private EasyDccSystemConnectionMemo memo = null;


    @Test
    public void testCTor() {
        // infrastructure objects
        EasyDccProgrammer p = new EasyDccProgrammer(memo);
        EasyDccProgrammerManager t = new EasyDccProgrammerManager(p, memo);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new EasyDccTrafficControlScaffold(null);
        memo = new EasyDccSystemConnectionMemo(tc);
    }

    @AfterEach
    public void tearDown() {
        tc.terminateThreads();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EasyDccProgrammerManagerTest.class);

}
