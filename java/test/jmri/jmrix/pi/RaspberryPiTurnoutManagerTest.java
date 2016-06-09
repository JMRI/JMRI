package jmri.jmrix.pi;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * <P>
 * Tests for RaspberryPiTurnoutManager
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiTurnoutManagerTest {

   @Test
   public void ConstructorTest(){
       RaspberryPiTurnoutManager m = new RaspberryPiTurnoutManager("Pi");
       Assert.assertNotNull(m);
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
