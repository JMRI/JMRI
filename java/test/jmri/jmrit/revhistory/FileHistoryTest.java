package jmri.jmrit.revhistory;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmrit.revhistory package & jmrit.revhistory.FileHistory class.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class FileHistoryTest{

    @Test
    public void testCtor() {
        new FileHistory();
    }

    @Test
    public void testAdd2() {
        FileHistory r = new FileHistory();
        r.addOperation("load", "foo.xml", null);
        r.addOperation("load", "bar.xml", null);

        Assert.assertEquals(2, r.list.size());

        Assert.assertEquals("foo.xml", r.list.get(0).filename);
        Assert.assertEquals("bar.xml", r.list.get(1).filename);

        Assert.assertEquals("load", r.list.get(0).type);
        Assert.assertEquals("load", r.list.get(1).type);
    }

    @Test
    public void testNest() {
        FileHistory r1 = new FileHistory();
        r1.addOperation("load", "date 1", "file 1", null);

        Assert.assertEquals(1, r1.list.size());

        FileHistory r2 = new FileHistory();
        r2.addOperation("load", "date 2", "file 2", r1);

        Assert.assertEquals(1, r2.list.size());

        Assert.assertEquals("file 1", r2.list.get(0).history.list.get(0).filename);
        Assert.assertEquals("date 1", r2.list.get(0).history.list.get(0).date);
    }

    @Test
    public void testPurge() {
        FileHistory r1 = new FileHistory();
        r1.addOperation("load", "date 1", "file 1", null);

        FileHistory r2 = new FileHistory();
        r2.addOperation("load", "date 2", "file 2", r1);

        FileHistory r3 = new FileHistory();
        r3.addOperation("load", "date 3", "file 3", r2);

        Assert.assertEquals("pre-purge r3", r2, r3.list.get(0).history);
        Assert.assertEquals("pre-purge r2", r1, r2.list.get(0).history);
        Assert.assertEquals("pre-purge r1", null, r1.list.get(0).history);

        r3.purge(2);

        Assert.assertEquals("post-purge r3", r2, r3.list.get(0).history);
        Assert.assertEquals("post-purge r2", null, r2.list.get(0).history);
    }

    @Test
    public void testToString() {
        FileHistory r1 = new FileHistory();
        r1.addOperation("load", "date 1", "file 1", null);

        FileHistory r2 = new FileHistory();
        r2.addOperation("load", "date 2", "file 2", r1);
        r2.addOperation("load", "date 3", "file 3", null);

        String result = r2.toString();
        String expected = "date 2: load file 2\n"
                + "    date 1: load file 1\n"
                + "date 3: load file 3\n";

        Assert.assertEquals(expected, result);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
