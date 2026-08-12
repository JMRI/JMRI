package jmri.jmrit.ussctc;

import javax.swing.Action;
import javax.swing.JFrame;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Tests for classes in the jmri.jmrit.ussctc.OsIndicatorAction class
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2010
 */
public class OsIndicatorActionTest {

    @Test
    public void testFrameCreate() {
        Action a = new OsIndicatorAction("test");
        Assert.assertNotNull(a);
    }

    @Test
    @DisabledIfHeadless
    public void testActionCreateAndFire() {

        new OsIndicatorAction("test").actionPerformed(null);
        JFrame f = JFrameOperator.waitJFrame(Bundle.getMessage("TitleOsIndicator"), true, true);
        Assertions.assertNotNull(f);
        JUnitUtil.dispose(f);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRouteManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
