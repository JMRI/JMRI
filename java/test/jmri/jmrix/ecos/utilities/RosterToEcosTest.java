package jmri.jmrix.ecos.utilities;

import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the RosterToEcos class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class RosterToEcosTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("RosterToEcos constructor", new RosterToEcos(new EcosSystemConnectionMemo()));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
