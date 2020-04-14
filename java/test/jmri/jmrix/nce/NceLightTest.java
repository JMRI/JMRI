package jmri.jmrix.nce;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NceLightTest {

    private NceTrafficControlScaffold tcis = null;

    @Test
    public void testCTor() {
        NceLight t = new NceLight("NL1", tcis, new NceLightManager(tcis.getAdapterMemo()));
        Assert.assertNotNull("exists", t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new NceTrafficControlScaffold();
        tcis.setAdapterMemo(new NceSystemConnectionMemo());
        tcis.getAdapterMemo().setNceTrafficController(tcis);
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NceLightTest.class);
}
