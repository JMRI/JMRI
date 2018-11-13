package jmri.jmrit.timetable;

import org.junit.*;

/*
 * Tests for the TrainType Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TrainTypeTest {

    @Test
    public void testCreate() {
        new TrainType(1, 1, "", "#000000");  // NOI18N
    }

    @Test
    public void testSettersAndGetters() {
        TrainType t = new TrainType(1, 1, "", "#000000");  // NOI18N
        Assert.assertEquals(1, t.getTypeId());  // NOI18N
        Assert.assertEquals(1, t.getLayoutId());
        t.setTypeName("New Type");  // NOI18N
        Assert.assertEquals("New Type", t.getTypeName());  // NOI18N
        t.setTypeColor("#ffffff");  // NOI18N
        Assert.assertEquals("#ffffff", t.getTypeColor());  // NOI18N
        Assert.assertEquals("New Type", t.toString());  // NOI18N
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