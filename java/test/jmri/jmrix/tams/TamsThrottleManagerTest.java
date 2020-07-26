package jmri.jmrix.tams;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TamsThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase{

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",tm);
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        TamsTrafficController tc = new TamsInterfaceScaffold();
        TamsSystemConnectionMemo memo = new TamsSystemConnectionMemo(tc);  
        tm = new TamsThrottleManager(memo);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TamsThrottleManagerTest.class);

}
