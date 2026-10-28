package jmri.implementation;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Tests for Section class.
 *
 * @author Bob Jacobsen Copyright (C) 2017
 * @author Paul Bender Copyright (C) 2016
 **/
public class DefaultSectionTest {

   @Test
   public void testSysNameConstructor(){
      Assert.assertNotNull("Constructor", new DefaultSection("TS1"));
   }

   @Test
   public void testTwoNameStringConstructor(){
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

   @Test
   public void testAllocatedToClearedWhenFree() {
    Section s = new DefaultSection("TS1");
    Block b = new Block("IB1", "user");
    s.addBlock(b);
    Assert.assertNull("not held at first", s.getAllocatedTo());

    java.util.List<String> sectionEvents = new java.util.ArrayList<>();
    s.addPropertyChangeListener(e -> sectionEvents.add(e.getPropertyName()));
    java.util.List<Object> blockEvents = new java.util.ArrayList<>();
    b.addPropertyChangeListener(e -> {
        if (Block.PROPERTY_ALLOCATED.equals(e.getPropertyName())) {
            blockEvents.add(e.getNewValue());
        }
    });

    Object holder = new Object();
    s.setState(Section.FORWARD);
    s.setAllocatedTo(holder);
    Assert.assertSame("holder recorded", holder, s.getAllocatedTo());
    Assert.assertTrue("Section reports the holder", sectionEvents.contains(Section.PROPERTY_ALLOCATED_TO));
    sectionEvents.clear();
    s.setAllocatedTo(holder);
    Assert.assertTrue("setting the same holder again changes nothing", sectionEvents.isEmpty());

    s.setState(Section.REVERSE);
    Assert.assertSame("a change of direction keeps the holder", holder, s.getAllocatedTo());

    s.setState(Section.FREE);
    Assert.assertNull("cleared when set FREE", s.getAllocatedTo());
    Assert.assertTrue("Section reports the release", sectionEvents.contains(Section.PROPERTY_ALLOCATED_TO));
    Assert.assertTrue("blocks are not sent allocation events", blockEvents.isEmpty());
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
