package jmri;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Scale tests.
 * @author Dave Sand Copyright (C) 2018
 */
public class ScaleTest {

    @Test
    public void testDefaultScale() {
        Scale scale = new Scale();
        assertNotNull( scale, "exists");
        assertEquals("HO", scale.getScaleName());
        assertEquals("HO", scale.getUserName());
        assertEquals(87.1, scale.getScaleRatio(), .1);
        assertEquals(1/87.1d, scale.getScaleFactor(), .001);
    }

    @Test
    public void testSetUser() {
        Scale scale = new Scale();
        Exception ex = assertThrows(IllegalArgumentException.class,() -> scale.setUserName("G"));
        assertNotNull(ex);
        assertEquals("Duplicate scale user name: G", ex.getMessage());

        assertDoesNotThrow( () -> scale.setUserName("XYZ"));
        assertEquals("XYZ", scale.getUserName());
    }

    @Test
    public void testVetoUser() {
        Scale scale = new Scale();
        scale.addVetoableChangeListener("ScaleUserName", (PropertyChangeEvent evt) -> {
            throw new PropertyVetoException("Test UserName Veto", evt);
        });
        Exception ex = assertThrows(PropertyVetoException.class, () -> scale.setUserName("QRS") );
        assertNotNull(ex);
        assertEquals("Test UserName Veto", ex.getMessage());
        assertEquals("HO", scale.getUserName());
        JUnitAppender.assertWarnMessage("The user name change for HO scale to QRS was rejected: Reason: Test UserName Veto");
    }

    @Test
    public void testSetRatio() {
        Scale scale = new Scale();
        Exception ex = assertThrows(IllegalArgumentException.class,() -> scale.setScaleRatio(0.0));
        assertNotNull(ex);
        assertEquals("The scale ratio is less than 1", ex.getMessage());

        assertDoesNotThrow( () -> scale.setScaleRatio(40.0));
        assertEquals(.025, scale.getScaleFactor(), .001);
    }

    @Test
    public void testVetoRatio() {
        Scale scale = new Scale();
        scale.addVetoableChangeListener("ScaleRatio", (PropertyChangeEvent evt) -> {
            throw new PropertyVetoException("Test Ratio Veto", evt);
        });
        Exception ex = assertThrows(PropertyVetoException.class, () -> scale.setScaleRatio(123) );
        assertNotNull(ex);
        assertEquals("Test Ratio Veto", ex.getMessage());
        assertEquals(87.1, scale.getScaleRatio(), .1);
        JUnitAppender.assertWarnMessage("The ratio change for HO scale to 123.0 was rejected: Reason: Test Ratio Veto");
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
