package jmri.jmrit.logix;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.assertThat;
/*
 * @author Pete Cressman Copyright (C) 2021
 */
public class BlockSpeedInfoTest {

    @Test
    public void testCTor() {
        BlockSpeedInfo info = new BlockSpeedInfo("blockA", .33f, .99f, .66f, 5555, 6666.6f, 9, 11);
        assertThat(info).withFailMessage("exists").isNotNull();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
