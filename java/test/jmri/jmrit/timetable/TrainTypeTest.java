package jmri.jmrit.timetable;

import java.io.File;
import java.io.IOException;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/*
 * Tests for the TrainType Class
 * @author Dave Sand Copyright (C) 2018
 */
public class TrainTypeTest {

    @Test
    public void testCreate() {
        try {
            new TrainType(0);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "TypeAddFail");  // NOI18N
        }
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

    @BeforeEach
    public void setUp(@TempDir File folder) throws IOException {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));
    }

    @AfterEach
    public void tearDown() {
       // use reflection to reset the static file location.
       try {
            Class<?> c = jmri.jmrit.timetable.configurexml.TimeTableXml.TimeTableXmlFile.class;
            java.lang.reflect.Field f = c.getDeclaredField("fileLocation");
            f.setAccessible(true);
            f.set(new String(), null);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException x) {
            Assert.fail("Failed to reset TimeTableXml static fileLocation " + x);
        }
        jmri.util.JUnitUtil.tearDown();
    }
}
