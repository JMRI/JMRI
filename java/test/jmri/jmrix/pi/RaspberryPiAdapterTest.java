package jmri.jmrix.pi;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * <P>
 * Tests for RaspberryPiAdapter
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiAdapterTest {

   @Ignore
   @Test(expected = java.lang.UnsatisfiedLinkError.class) // only really works on 
                                                    // Pi for now.
   public void ConstructorTest(){
       RaspberryPiAdapter a = new RaspberryPiAdapter();
       Assert.assertNotNull(a);
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
