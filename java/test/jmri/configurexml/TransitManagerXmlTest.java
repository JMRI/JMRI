package jmri.configurexml;

import jmri.*;
import jmri.managers.DefaultTransitManager;
import jmri.managers.configurexml.DefaultTransitManagerXml;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Tests for TransitManagerXml class.
 *
 * @author Bob Jacobsen Copyright (C) 2017
 * @author Paul Bender Copyright (C) 2016
 **/

public class TransitManagerXmlTest {

   @Test
   public void testCtor(){
      Assert.assertNotNull("Constructor", new DefaultTransitManagerXml());
   }

   @Test
   public void testNoElementIfEmpty(){
      var tmx = new DefaultTransitManagerXml();
      TransitManager tm = new DefaultTransitManager();
      Assert.assertNull("No elements", tmx.store(tm));
   }

   @Test
   public void testStoreOneTransit() throws Exception {
      var tmx = new DefaultTransitManagerXml();
      TransitManager tm = new DefaultTransitManager();
      Transit t = tm.createNewTransit("TS1", "user");

      Section s = new jmri.implementation.DefaultSection("SS1");
      TransitSection ts = new TransitSection(s,0,0,false);

      TransitSectionAction ta = new TransitSectionAction(0,0);
      ts.addAction(ta);

      t.addTransitSection(ts);

      org.jdom2.Element e = tmx.store(tm);
      Assert.assertNotNull("Element(s) returned", e );

      Assert.assertNotNull("Element(s) processed", tmx.load(e, null));
   }

   @BeforeEach
   public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
   }

   @AfterEach
   public void tearDown(){
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
   }

}
