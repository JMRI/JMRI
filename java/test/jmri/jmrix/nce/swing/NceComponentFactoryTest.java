package jmri.jmrix.nce.swing;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.nce.NceTrafficControlScaffold;
import jmri.jmrix.nce.NceSystemConnectionMemo;

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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        tcis = new NceTrafficControlScaffold();
        memo = new NceSystemConnectionMemo();
        memo.setNceTrafficController(tcis);
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(NceComponentFactoryTest.class.getName());

}
