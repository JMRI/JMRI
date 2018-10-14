package jmri.jmrit.timetable;

import org.junit.*;

/*
 * Tests for the TrainType Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TrainTypeTest {

    @Test
    public void testCreate() {
        new TrainType(1, 1, "", "#000000");
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}