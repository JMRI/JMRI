package jmri.jmrix.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the JmrixConfigPaneXml class.
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class JmrixConfigPaneXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("JmrixConfigPaneXml constructor", new JmrixConfigPaneXml());
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
