package jmri.jmrix.pi;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <P>
 * Tests for RaspberryPiSensorManager
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiSensorManagerTest extends jmri.managers.AbstractSensorMgrTest {

    @Override
    public String getSystemName(int i) {
        return "PiS" + i;
    }


   @Test
   public void ConstructorTest(){
       Assert.assertNotNull(l);
   }

   @Test
   public void checkPrefix(){
       Assert.assertEquals("Prefix","Pi",l.getSystemPrefix());
   }

    // The minimal setup for log4J
    @Before
    public void setUp() {
       apps.tests.Log4JFixture.setUp();
       jmri.util.JUnitUtil.resetInstanceManager();
       l = new RaspberryPiSensorManager("Pi");
    }

    @After
    public void tearDown() {
       jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }


}
