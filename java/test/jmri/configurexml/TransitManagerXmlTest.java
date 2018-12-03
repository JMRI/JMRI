package jmri.configurexml;

import jmri.*;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
      Assert.assertNotNull("Constructor", new TransitManagerXml());
   }

   @Test
   public void NoElementIfEmptyTest(){
      TransitManagerXml tmx = new TransitManagerXml();
      TransitManager tm = new TransitManager();
      Assert.assertNull("No elements", tmx.store(tm));
   }

   @Test
   public void StoreOneTransitTest() throws Exception {
      TransitManagerXml tmx = new TransitManagerXml();
      TransitManager tm = new TransitManager();
      Transit t = tm.createNewTransit("TS1", "user");
      
      Section s = new Section("SS1");
      TransitSection ts = new TransitSection(s,0,0,false);
      
      TransitSectionAction ta = new TransitSectionAction(0,0);
      ts.addAction(ta);
      
      t.addTransitSection(ts);
      
      org.jdom2.Element e;
      Assert.assertNotNull("Element(s) returned", e = tmx.store(tm));

      Assert.assertNotNull("Element(s) processed", tmx.load(e, null));
   }

   @Before
   public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
   }

   @After
   public void tearDown(){
        JUnitUtil.tearDown();
   }

}
