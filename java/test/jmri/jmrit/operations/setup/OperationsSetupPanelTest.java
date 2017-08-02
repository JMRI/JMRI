package jmri.jmrit.operations.setup;

<<<<<<< HEAD
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
=======
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
public class OperationsSetupPanelTest {

    @Test
    public void testCTor() {
        OperationsSetupPanel t = new OperationsSetupPanel();
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
<<<<<<< HEAD
        jmri.util.JUnitUtil.resetInstanceManager();
=======
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
>>>>>>> JMRI/master
    }

    @After
    public void tearDown() {
<<<<<<< HEAD
        jmri.util.JUnitUtil.resetInstanceManager();
=======
        JUnitUtil.resetInstanceManager();
>>>>>>> JMRI/master
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(OperationsSetupPanelTest.class.getName());

}
