package jmri.jmrix.ecos;

import jmri.jmrix.AbstractPowerManagerTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EcosPowerManagerTest {

    @Test
    public void testCTor() throws Exception {
        EcosTrafficController tc = new EcosInterfaceScaffold();
        EcosPowerManager t = new EcosPowerManager(tc);
        Assert.assertNotNull("exists",t);

        t.dispose();
        tc.terminateThreads();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EcosPowerManagerTest.class);

}
