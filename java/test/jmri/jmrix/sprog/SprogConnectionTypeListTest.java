package jmri.jmrix.sprog;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * <P>
 * Tests for SprogConnectionTypeList
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogConnectionTypeListTest {

   @Test
   public void ConstructorTest(){
       SprogConnectionTypeList ct = new SprogConnectionTypeList();
       Assert.assertNotNull(ct);
   }

   @Test
   public void ManfacturerString(){
       SprogConnectionTypeList ct = new SprogConnectionTypeList();
       Assert.assertEquals("Manufacturers",new String[]{"SPROG DCC"},ct.getManufacturers());
   }

   @Test
   public void ProtocolClassList(){
       SprogConnectionTypeList ct = new SprogConnectionTypeList();
       Assert.assertEquals("Protocol Class List",new String[]{
            "jmri.jmrix.sprog.sprog.ConnectionConfig",
            "jmri.jmrix.sprog.sprogCS.ConnectionConfig",
            "jmri.jmrix.sprog.sprognano.ConnectionConfig",
            "jmri.jmrix.sprog.pi.pisprogone.ConnectionConfig",
            "jmri.jmrix.sprog.pi.pisprogonecs.ConnectionConfig",
            "jmri.jmrix.sprog.pi.pisprognano.ConnectionConfig"},
            ct.getAvailableProtocolClasses());
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
