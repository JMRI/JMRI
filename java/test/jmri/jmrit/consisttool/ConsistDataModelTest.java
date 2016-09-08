package jmri.jmrit.consisttool;

import apps.tests.Log4JFixture;
import jmri.ConsistManager;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of ConsistDataModel
 *
 * @author	Paul Bender Copyright (C) 2015,2016
 */
public class ConsistDataModelTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConsistDataModel model = new ConsistDataModel(1,4);
        Assert.assertNotNull("exists", model);
    }

    @Before
    public void setUp() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.setUp();
        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager());
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }


}
