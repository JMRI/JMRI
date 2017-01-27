package jmri.configurexml;

import jmri.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for TransitManagerXml class.
 *
 * @author Bob Jacobsen Copyright (C) 2017
 * @author Paul Bender Copyright (C) 2016
 **/

public class TransitManagerXmlTest {

   @Test
   public void BaseTest(){
      TransitManagerXml tmx;
      Assert.assertNotNull("Constructor", tmx = new TransitManagerXml());
   }

   @Test
   public void NoElementIfEmptyTest(){
      TransitManagerXml tmx = new TransitManagerXml();
      TransitManager tm = new TransitManager();
      Assert.assertNull("No elements", tmx.store(tm));
   }

   @Test
   public void StoreOneTransitTest(){
      TransitManagerXml tmx = new TransitManagerXml();
      TransitManager tm = new TransitManager();
      Transit t = tm.createNewTransit("TS1", "user");
      
      Section s = new Section("SS1");
      TransitSection ts = new TransitSection(s,0,0,false);
      
      TransitSectionAction ta = new TransitSectionAction(0,0);
      ts.addAction(ta);
      
      t.addTransitSection(ts);
      
      Assert.assertNotNull("Element(s) returned", tmx.store(tm));
   }

   @Before
   public void setUp(){
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
   }

   @After
   public void tearDown(){
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
   }

}
