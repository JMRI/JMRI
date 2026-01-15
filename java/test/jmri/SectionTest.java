package jmri;

import jmri.util.JUnitUtil;

import jmri.implementation.DefaultSection;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for Section class.
 *
 * @author Bob Jacobsen Copyright (C) 2017
 * @author Paul Bender Copyright (C) 2016
 **/
public class SectionTest {

    @Test
    public void testSectionSysNameConstructorTest(){
        assertNotNull( new DefaultSection("TS1"), "Constructor");
    }

    @Test
    public void testSectionTwoNameStringConstructorTest(){
        assertNotNull( new DefaultSection("TS1", "user name"), "Constructor");
    }

    @Test
    public void testWarnOnBlockAdd() {
        Section  s = new DefaultSection("TS1");
        assertEquals(0, s.getBlockList().size());
        s.addBlock(new Block("IB1", "user"));
        assertEquals(1, s.getBlockList().size());
    }

    @Test
    public void testWarnOnBlockAddWithNoUserName() {
        Section  s = new DefaultSection("TS1");
        assertEquals(0, s.getBlockList().size());
        s.addBlock(new Block("IB1"));
        jmri.util.JUnitAppender.assertWarnMessage("Block IB1 does not have a user name, may not work correctly in Section TS1");
        assertEquals(1, s.getBlockList().size());
    }

   @BeforeEach
   public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
   }

   @AfterEach
   public void tearDown(){
        JUnitUtil.tearDown();
   }

}
