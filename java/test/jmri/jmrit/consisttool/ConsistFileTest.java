package jmri.jmrit.consisttool;

import apps.tests.Log4JFixture;
import jmri.ConsistManager;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of ConsistFile
 *
 * @author	Paul Bender Copyright (C) 2015
 */
public class ConsistFileTest {

    @Test
    public void testCtor() {
        ConsistFile file = new ConsistFile();
        Assert.assertNotNull("exists", file);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager());
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
