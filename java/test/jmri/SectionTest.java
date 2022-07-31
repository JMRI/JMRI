package jmri;

import jmri.util.JUnitUtil;

import jmri.implementation.DefaultSection;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Tests for Section class.
 *
 * @author Bob Jacobsen Copyright (C) 2017
 * @author Paul Bender Copyright (C) 2016
 **/

public class SectionTest {

   @Test
   public void testSectionSysNameConstructorTest(){
      Assert.assertNotNull("Constructor", new DefaultSection("TS1"));
   }

   @Test
   public void testSectionTwoNameStringConstructorTest(){
      Assert.assertNotNull("Constructor", new DefaultSection("TS1", "user name"));
   }

   @Test
   public void testWarnOnBlockAdd() {
    Section  s = new DefaultSection("TS1");
    Assert.assertEquals(0, s.getBlockList().size());
    s.addBlock(new Block("IB1", "user"));
    Assert.assertEquals(1, s.getBlockList().size());
   }

   @Test
   public void testWarnOnBlockAddWithNoUserName() {
    Section  s = new DefaultSection("TS1");
    Assert.assertEquals(0, s.getBlockList().size());
    s.addBlock(new Block("IB1"));
    jmri.util.JUnitAppender.assertWarnMessage("Block IB1 does not have a user name, may not work correctly in Section TS1");
    Assert.assertEquals(1, s.getBlockList().size());
   }

   @BeforeEach
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
   }

   @AfterEach
   public void tearDown(){
        JUnitUtil.tearDown();
   }

}
