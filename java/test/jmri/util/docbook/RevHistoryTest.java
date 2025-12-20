package jmri.util.docbook;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for the jmri.util.docbook.RevHistory class.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class RevHistoryTest {

    @Test
    public void testCtor() {
        assertNotNull(new RevHistory());
    }

    @Test
    public void testAdd2() {
        RevHistory r = new RevHistory();
        r.addRevision("one");
        r.addRevision("two");

        assertEquals(2, r.list.size());

        assertEquals(1, r.list.get(0).revnumber);
        assertEquals(2, r.list.get(1).revnumber);

        assertEquals("one", r.list.get(0).revremark);
        assertEquals("two", r.list.get(1).revremark);
    }

    @Test
    public void testToString() {
        RevHistory r2 = new RevHistory();
        r2.addRevision(2, "date 2", "initials 2", "remark 2");
        r2.addRevision(3, "date 3", "initials 3", "remark 3");

        String result = r2.toString(" ");
        String expected = " 2, date 2, initials 2, remark 2\n"
                + " 3, date 3, initials 3, remark 3\n";

        assertEquals(expected, result);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
