package jmri.jmrit.display.switchboardEditor.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the SwitchboardEditorXml class
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SwitchboardEditorXmlTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("SwitchboardEditorXml constructor", new SwitchboardEditorXml());
    }

    @Test
    public void testBeanSwitchXmlCtor() {
        BeanSwitchXml t = new BeanSwitchXml();
        Assert.assertNotNull("exists", t);
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
