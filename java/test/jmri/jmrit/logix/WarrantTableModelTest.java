package jmri.jmrit.logix;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
<<<<<<< HEAD
import org.junit.Ignore;
=======
>>>>>>> JMRI/master
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GraphicsEnvironment;

/**
 *
<<<<<<< HEAD
 * @author Paul Bender Copyright (C) 2017	
=======
 * @author Paul Bender Copyright (C) 2017
>>>>>>> JMRI/master
 */
public class WarrantTableModelTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
<<<<<<< HEAD
        WarrantTableFrame f = WarrantTableFrame.getInstance();
=======
        WarrantTableFrame f = WarrantTableFrame.getDefault();
>>>>>>> JMRI/master
        WarrantTableModel t = new WarrantTableModel(f);
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

    private final static Logger log = LoggerFactory.getLogger(WarrantTableModelTest.class.getName());

}
