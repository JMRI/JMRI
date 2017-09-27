package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <P>
 * Tests for RaspberryPiAdapter
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiAdapterTest {

   @Test
   public void ConstructorTest(){
       RaspberryPiAdapter a = new RaspberryPiAdapter();
       Assert.assertNotNull(a);
   }

    // The minimal setup for log4J
    @Before
    public void setUp() {
       apps.tests.Log4JFixture.setUp();
       GpioProvider myprovider = new PiGpioProviderScaffold();
       GpioFactory.setDefaultProvider(myprovider);
       jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }


}
