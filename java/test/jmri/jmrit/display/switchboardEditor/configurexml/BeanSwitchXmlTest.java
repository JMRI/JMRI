package jmri.jmrit.display.switchboardEditor.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the SwitchboardEditorXml class
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class BeanSwitchXmlTest {

    @Test
    public void testCtor() {
        BeanSwitchXml t = new BeanSwitchXml();
        Assertions.assertNotNull(t, "exists");
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
