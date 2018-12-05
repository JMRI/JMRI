package jmri;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Scale tests.
 * @author Dave Sand Copyright (C) 2018
 */
public class ScaleTest {

    @Test
    public void testDefaultScale() {
        Scale scale = new Scale();
        Assert.assertNotNull("exists", scale);
        Assert.assertEquals(scale.getScaleName(), "HO");
        Assert.assertEquals(scale.getUserName(), "HO");
        Assert.assertEquals(scale.getScaleRatio(), 87.1, .1);
        Assert.assertEquals(scale.getScaleFactor(), .011, .001);
    }

    @Test
    public void testSetUser() {
        Scale scale = new Scale();
        try {
            scale.setUserName("G");
        } catch (Exception ex) {
            Assert.assertEquals(ex.getMessage(), "Duplicate scale user name");
        }
        try {
            scale.setUserName("XYZ");
        } catch (Exception ex) {
        }
        Assert.assertEquals(scale.getUserName(), "XYZ");
    }

    @Test
    public void testVetoUser() {
        Scale scale = new Scale();
        scale.addVetoableChangeListener("ScaleUserName", new VetoableChangeListener() {
            @Override
            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                throw new PropertyVetoException("Test UserName Veto", evt);
            }
        });
        try {
            scale.setUserName("QRS");
        } catch (Exception ex) {
            log.debug("ex: {}", ex.getMessage());
        }
        Assert.assertEquals(scale.getUserName(), "HO");
        jmri.util.JUnitAppender.assertWarnMessage("The user name change for HO scale to QRS was rejected: Reason: Test UserName Veto");
    }

    @Test
    public void testSetRatio() {
        Scale scale = new Scale();
        try {
            scale.setScaleRatio(0.0);
        } catch (Exception ex) {
            Assert.assertEquals(ex.getMessage(), "The scale ratio is less than 1");
        }
        try {
            scale.setScaleRatio(40.0);
        } catch (Exception ex) {
        }
        Assert.assertEquals(scale.getScaleFactor(), .025, .001);
    }

    @Test
    public void testVetoRatio() {
        Scale scale = new Scale();
        scale.addVetoableChangeListener("ScaleRatio", new VetoableChangeListener() {
            @Override
            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                throw new PropertyVetoException("Test Ratio Veto", evt);
            }
        });
        try {
            scale.setScaleRatio(123);
        } catch (Exception ex) {
            log.debug("ex: {}", ex.getMessage());
        }
        Assert.assertEquals(scale.getScaleRatio(), 87.1, .1);
        jmri.util.JUnitAppender.assertWarnMessage("The ratio change for HO scale to 123.0 was rejected: Reason: Test Ratio Veto");
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ScaleTest.class);
}
