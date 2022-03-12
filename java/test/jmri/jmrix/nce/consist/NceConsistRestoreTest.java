package jmri.jmrix.nce.consist;

import jmri.jmrix.nce.NceTrafficControlScaffold;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NceConsistRestoreTest {

    private NceTrafficControlScaffold tcis = null;

    @Test
    public void testCTor() {
        NceConsistRestore t = new NceConsistRestore(tcis);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new NceTrafficControlScaffold();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NceConsistRestoreTest.class);

}
