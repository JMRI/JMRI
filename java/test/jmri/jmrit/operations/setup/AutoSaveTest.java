package jmri.jmrit.operations.setup;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
<<<<<<< HEAD
import org.junit.Ignore;
=======
>>>>>>> JMRI/master
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
<<<<<<< HEAD
 * @author Paul Bender Copyright (C) 2017	
=======
 * @author Paul Bender Copyright (C) 2017
>>>>>>> JMRI/master
 */
public class AutoSaveTest {

    @Test
    public void testCTor() {
        AutoSave t = new AutoSave();
<<<<<<< HEAD
        Assert.assertNotNull("exists",t);
=======
        Assert.assertNotNull("exists", t);
        t.stop();
>>>>>>> JMRI/master
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

    private final static Logger log = LoggerFactory.getLogger(AutoSaveTest.class.getName());

}
