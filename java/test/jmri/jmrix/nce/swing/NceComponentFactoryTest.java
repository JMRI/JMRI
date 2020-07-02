package jmri.jmrix.nce.swing;

import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficControlScaffold;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NceComponentFactoryTest {

    private NceTrafficControlScaffold tcis = null;
    private NceSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        NceComponentFactory t = new NceComponentFactory(memo);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new NceTrafficControlScaffold();
        memo = new NceSystemConnectionMemo();
        memo.setNceTrafficController(tcis);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NceComponentFactoryTest.class);

}
