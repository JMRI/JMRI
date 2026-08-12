package jmri.jmrix.jinput.treecontrol;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of TreeAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class TreeActionTest {

    @Test
    @DisabledIfHeadless
    public void testStringCtor() {
        TreeAction action = new TreeAction("Light Control Action");
        Assertions.assertNotNull(action, "exists");
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
