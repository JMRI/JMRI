package jmri.jmrix.rps;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * JUnit tests for the rps.Engine class.
 *
 * @author	Bob Jacobsen Copyright 2008
 */
public class EngineTest {

    @Test
    public void testCtor() {
        Engine e = new Engine();
        Assert.assertNotNull("exists", e);
    }

    @Test
    public void testNumReceivers() {
        Engine e = new Engine();
        e.setMaxReceiverNumber(3);
        Assert.assertEquals("number", 3, e.getMaxReceiverNumber());
    }

   @Before
   public void setUp() {
        jmri.util.JUnitUtil.setUp();
   }

   @After
   public void tearDown(){
        jmri.util.JUnitUtil.tearDown();
   }

}
