package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the WarrantWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class WarrantWhereUsedTest {

    @Test
    public void testWarrantWhereUsed() {
        WarrantWhereUsed ctor = new WarrantWhereUsed();
        Assert.assertNotNull("exists", ctor);
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
