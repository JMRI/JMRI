package jmri.jmrit.timetable.configurexml;

import java.awt.GraphicsEnvironment;
import org.junit.*;
import jmri.jmrit.timetable.*;
import jmri.jmrit.timetable.swing.*;
import jmri.util.JUnitUtil;

/**
 * Tests for the TimeTableXml Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TimeTableXmlTest {

    @Test
    public void testCreate() {
        new TimeTableXml();
    }

    @Test
    public void testLoadAndStore() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TimeTableFrame f = new TimeTableFrame("");
        Assert.assertNotNull(f);
        boolean loadResult = TimeTableXml.doLoad();
        Assert.assertTrue("Load Failed", loadResult);  // NOI18N
        boolean storeResult = TimeTableXml.doStore();
        Assert.assertTrue("Store Failed", storeResult);  // NOI18N
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}