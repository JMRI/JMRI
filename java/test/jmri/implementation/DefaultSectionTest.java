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

   @Test
   public void testStopPoints() {
    Section s = new DefaultSection("TS1");
    Assert.assertEquals("none at first", 0.0f, s.getStopPointMm(Section.FORWARD), 0.0f);
    Assert.assertEquals("none at first", 0.0f, s.getStopPointMm(Section.REVERSE), 0.0f);
    s.setStopPointMm(Section.FORWARD, 420.5f);
    s.setStopPointMm(Section.REVERSE, 150.0f);
    Assert.assertEquals(420.5f, s.getStopPointMm(Section.FORWARD), 0.0f);
    Assert.assertEquals(150.0f, s.getStopPointMm(Section.REVERSE), 0.0f);
    Assert.assertEquals("kept as a property, so it is saved", 420.5f, s.getProperty(Section.STOP_POINT_FORWARD));
    s.setStopPointMm(Section.FORWARD, 0.0f);
    Assert.assertEquals("0 clears it", 0.0f, s.getStopPointMm(Section.FORWARD), 0.0f);
    Assert.assertFalse("and removes the property", s.getPropertyKeys().contains(Section.STOP_POINT_FORWARD));
    Assert.assertEquals("the other direction is kept", 150.0f, s.getStopPointMm(Section.REVERSE), 0.0f);
   }

   @Test
   public void testValidateStopPoints() {
    DefaultSection s = new DefaultSection("TS1");
    jmri.Block a = new jmri.Block("IB1", "A");
    jmri.Block b = new jmri.Block("IB2", "B");
    s.addBlock(a);
    s.addBlock(b);
    Assert.assertEquals("no stop point: nothing to check", "", s.validateStopPoints());
    s.setStopPointMm(Section.FORWARD, 450.0f);
    Assert.assertEquals("a Block with no length: can't be checked", "", s.validateStopPoints());
    jmri.util.JUnitAppender.assertWarnMessageStartsWith("Section TS1 has a stop point, but its Block A (IB1) has no length");
    a.setLength(300.0f);
    b.setLength(200.0f);
    Assert.assertEquals("inside the Section", "", s.validateStopPoints());
    s.setStopPointMm(Section.REVERSE, 520.0f);
    Assert.assertEquals("Reverse stop point 520mm is beyond the end of Section TS1, whose Blocks are 500mm long.",
            s.validateStopPoints());
    s.setStopPointMm(Section.REVERSE, 500.0f);
    Assert.assertEquals("at the far end is inside", "", s.validateStopPoints());
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
