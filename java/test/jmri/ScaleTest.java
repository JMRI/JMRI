package jmri;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Scale tests.
 * @author Dave Sand Copyright (C) 2018
 */
public class ScaleTest {

    @Test
    public void testDefaultScale() {
        Scale scale = new Scale();
        Assert.assertNotNull("exists", scale);
        Assert.assertEquals("HO", scale.getScaleName());
        Assert.assertEquals("HO", scale.getUserName());
        Assert.assertEquals(scale.getScaleRatio(), 87.1, .1);
        Assert.assertEquals(scale.getScaleFactor(), .011, .001);
    }

    @Test
    public void testSetUser() {
        Scale scale = new Scale();
        try {
            scale.setUserName("G");
            Assertions.fail("scale username set to G");
        } catch (PropertyVetoException | IllegalArgumentException ex) {
            Assert.assertEquals("Duplicate scale user name", ex.getMessage());
        }
        try {
            scale.setUserName("XYZ");
        } catch (PropertyVetoException | IllegalArgumentException ex) {
            Assertions.fail("Could not set username ", ex);
        }
        Assert.assertEquals("XYZ", scale.getUserName());
    }

    @Test
    public void testVetoUser() {
        Scale scale = new Scale();
        scale.addVetoableChangeListener("ScaleUserName", (PropertyChangeEvent evt) -> {
            throw new PropertyVetoException("Test UserName Veto", evt);
        });
        try {
            scale.setUserName("QRS");
        } catch (PropertyVetoException | IllegalArgumentException ex) {
            Assert.assertEquals("Test UserName Veto", ex.getMessage());
        }
        Assert.assertEquals("HO", scale.getUserName());
        jmri.util.JUnitAppender.assertWarnMessage("The user name change for HO scale to QRS was rejected: Reason: Test UserName Veto");
    }

    @Test
    public void testSetRatio() {
        Scale scale = new Scale();
        try {
            scale.setScaleRatio(0.0);
        } catch (PropertyVetoException | IllegalArgumentException ex) {
            Assert.assertEquals("The scale ratio is less than 1", ex.getMessage());
        }
        try {
            scale.setScaleRatio(40.0);
        } catch (PropertyVetoException | IllegalArgumentException ex) {
            Assertions.fail("Could not set setScaleRatio to 40 ", ex);
        }
        Assert.assertEquals(scale.getScaleFactor(), .025, .001);
    }

    @Test
    public void testVetoRatio() {
        Scale scale = new Scale();
        scale.addVetoableChangeListener("ScaleRatio", (PropertyChangeEvent evt) -> {
            throw new PropertyVetoException("Test Ratio Veto", evt);
        });
        try {
            scale.setScaleRatio(123);
        } catch (PropertyVetoException | IllegalArgumentException ex) {
            Assert.assertEquals("Test Ratio Veto", ex.getMessage());
        }
        Assert.assertEquals(scale.getScaleRatio(), 87.1, .1);
        jmri.util.JUnitAppender.assertWarnMessage("The ratio change for HO scale to 123.0 was rejected: Reason: Test Ratio Veto");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ScaleTest.class);
}
