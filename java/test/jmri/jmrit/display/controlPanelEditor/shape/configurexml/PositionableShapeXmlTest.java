package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import jmri.util.JUnitUtil;

import org.jdom2.Element;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * PositionableShapeXmlTest.java
 * <p>
 * Test for the PositionableShapeXml class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PositionableShapeXmlTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("PositionableShapeXml constructor", new PositionableShapeXml() {
            @Override
            public void load(Element e, Object o) {
                // do nothing
            }
        });
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
