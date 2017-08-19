package jmri.web.servlet.operations;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.InstanceManager;
import jmri.jmrit.operations.trains.TrainManager;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class HtmlConductorTest {

    @Test
    public void testCTor() throws java.io.IOException {
        HtmlConductor t = new HtmlConductor(java.util.Locale.US,
                     (InstanceManager.getDefault(TrainManager.class)).getTrainById("2"));
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitOperationsUtil.resetOperationsManager();
        jmri.util.JUnitOperationsUtil.initOperationsData();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(HtmlConductorTest.class.getName());

}
