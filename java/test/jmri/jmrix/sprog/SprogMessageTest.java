package jmri.jmrix.sprog;

import jmri.managers.DefaultProgrammerManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the SprogMessage class
 *
 * @author	Bob Jacobsen Copyright 2012
 * @version	$Revision$
 */
public class SprogMessageTest{

    @Test
    public void testCreate() {
        SprogMessage m = new SprogMessage(1);
        Assert.assertNotNull("exists", m);
    }

    @Test
    public void testReadCv() {
        SprogMessage m = SprogMessage.getReadCV(12, DefaultProgrammerManager.PAGEMODE);
        Assert.assertEquals("string compare ", "V 0012", m.toString());
    }

    @Test
    public void testWriteCV() {
        SprogMessage m = SprogMessage.getWriteCV(12, 251, DefaultProgrammerManager.PAGEMODE);
        Assert.assertEquals("string compare ", "V 0012 251", m.toString());
    }

    @Test
    public void testReadCvLarge() {
        SprogMessage m = SprogMessage.getReadCV(1021, DefaultProgrammerManager.PAGEMODE);
        Assert.assertEquals("string compare ", "V 1021", m.toString());
    }

    @Test
    public void testWriteCVLarge() {
        SprogMessage m = SprogMessage.getWriteCV(1021, 251, DefaultProgrammerManager.PAGEMODE);
        Assert.assertEquals("string compare ", "V 1021 251", m.toString());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }


}
