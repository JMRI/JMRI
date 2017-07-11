package jmri.jmrix.nce;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class NceConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase {

    private NceTrafficControlScaffold tcis = null;
    private NceSystemConnectionMemo memo = null;

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        tcis = new NceTrafficControlScaffold();
        memo = new NceSystemConnectionMemo();
        memo.setNceTrafficController(tcis);
        cm = new NceConsistManager(memo);
    }

    @After
    @Override
    public void tearDown() {
        cm = null;
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(NceConsistManagerTest.class.getName());

}
