package jmri.managers;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class InternalTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    /** {@inheritDoc} */
    @Override
    public String getSystemName(int i) {
        return "IT" + i;
    }

    @Test
    public void testAsAbstractFactory() {

        // ask for a Turnout, and check type
        Turnout tl = l.newTurnout("IT21", "my name");

        Assert.assertTrue(null != tl);

        // make sure loaded into tables
        Assert.assertTrue(null != l.getBySystemName("IT21"));
        Assert.assertTrue(null != l.getByUserName("my name"));

    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.resetInstanceManager();
        l = new InternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(InternalTurnoutManagerTest.class);

}
