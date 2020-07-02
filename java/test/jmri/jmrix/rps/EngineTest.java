package jmri.jmrix.rps;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * JUnit tests for the rps.Engine class.
 *
 * @author Bob Jacobsen Copyright 2008
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

   @BeforeEach
   public void setUp() {
        jmri.util.JUnitUtil.setUp();
   }

   @AfterEach
   public void tearDown(){
        jmri.util.JUnitUtil.tearDown();
   }

}
