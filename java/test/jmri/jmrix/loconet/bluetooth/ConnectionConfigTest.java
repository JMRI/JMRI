package jmri.jmrix.loconet.bluetooth;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.ToDo;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for ConnectionConfig class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class ConnectionConfigTest extends jmri.jmrix.AbstractSerialConnectionConfigTestBase  {

   @Before
   @Override
   public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new ConnectionConfig();
   }
   
   @Test
   @Override
   @Ignore("null pointer exception if run as single or package level test")
   @ToDo("investigate null pointer exception on getPortNames ")
   public void testLoadDetails() {
       super.testLoadDetails();
   }

   @After
   @Override
   public void tearDown(){
        cc = null;
        JUnitUtil.tearDown();
   }

}
