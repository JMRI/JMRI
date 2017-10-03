package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import jmri.util.JUnitUtil;
import org.jdom2.Element;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * PositionableShapeXmlTest.java
 * <p>
 * Description: tests for the PositionableShapeXml class
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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
