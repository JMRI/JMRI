package jmri.util.docbook;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Tests for the jmri.util.docbook.RevHistory class.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class RevHistoryTest {

    @Test
    public void testCtor() {
        new RevHistory();
    }

    @Test
    public void testAdd2() {
        RevHistory r = new RevHistory();
        r.addRevision("one");
        r.addRevision("two");

        Assert.assertEquals(2, r.list.size());

        Assert.assertEquals(1, r.list.get(0).revnumber);
        Assert.assertEquals(2, r.list.get(1).revnumber);

        Assert.assertEquals("one", r.list.get(0).revremark);
        Assert.assertEquals("two", r.list.get(1).revremark);
    }

    @Test
    public void testToString() {
        RevHistory r2 = new RevHistory();
        r2.addRevision(2, "date 2", "initials 2", "remark 2");
        r2.addRevision(3, "date 3", "initials 3", "remark 3");

        String result = r2.toString(" ");
        String expected = " 2, date 2, initials 2, remark 2\n"
                + " 3, date 3, initials 3, remark 3\n";

        Assert.assertEquals(expected, result);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
