package jmri.jmrix.nce;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NceLightManagerTest {

    private NceTrafficControlScaffold tcis = null;

    @Test
    public void testCTor() {
        NceLightManager t = new NceLightManager(tcis.getAdapterMemo());
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new NceTrafficControlScaffold();
        tcis.setAdapterMemo(new NceSystemConnectionMemo());
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NceLightManagerTest.class);

}
