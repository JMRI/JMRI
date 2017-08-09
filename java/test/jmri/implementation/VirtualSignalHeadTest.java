package jmri.implementation;

<<<<<<< HEAD
=======
import jmri.SignalHead;

>>>>>>> JMRI/master
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
<<<<<<< HEAD
public class VirtualSignalHeadTest {
=======
public class VirtualSignalHeadTest extends AbstractSignalHeadTestBase {
>>>>>>> JMRI/master

    @Test
    public void testCTor() {
        VirtualSignalHead t = new VirtualSignalHead("Virtual Signal Head Test");
        Assert.assertNotNull("exists",t);
    }

<<<<<<< HEAD
=======
    @Override
    public SignalHead getHeadToTest() {
        return new VirtualSignalHead("Virtual Signal Head Test");
    }

>>>>>>> JMRI/master
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

<<<<<<< HEAD
    private final static Logger log = LoggerFactory.getLogger(VirtualSignalHeadTest.class.getName());
=======
    //private final static Logger log = LoggerFactory.getLogger(VirtualSignalHeadTest.class.getName());
>>>>>>> JMRI/master

}
