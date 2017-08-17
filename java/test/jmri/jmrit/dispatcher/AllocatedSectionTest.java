package jmri.jmrit.dispatcher;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AllocatedSectionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.Transit transit = new jmri.Transit("TT1");
        ActiveTrain at = new ActiveTrain(transit,"Train",ActiveTrain.USER);
        jmri.Section section1 = new jmri.Section("TS1");
        jmri.Section section2 = new jmri.Section("TS2");
        AllocatedSection t = new AllocatedSection(section1,at,1,section2,2);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AllocatedSectionTest.class.getName());

}
