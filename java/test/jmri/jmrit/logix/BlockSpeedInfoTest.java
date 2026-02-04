package jmri.jmrit.logix;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;

/*
 * @author Pete Cressman Copyright (C) 2021
 */
public class BlockSpeedInfoTest {

    @Test
    public void testCTor() {
        BlockSpeedInfo info = new BlockSpeedInfo("blockA", .33f, .99f, 5555, .66f, 6666.6f, 9, 11);
        Assertions.assertNotNull(info, "exists");
        Assertions.assertNotNull(info.toString(),"toString");
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
