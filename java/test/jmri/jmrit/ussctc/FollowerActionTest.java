package jmri.jmrit.ussctc;

import java.awt.GraphicsEnvironment;

import javax.swing.Action;
import javax.swing.JFrame;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Tests for classes in the jmri.jmrit.ussctc.FollowerAction class
 *
 * @author Bob Jacobsen Copyright 2003, 2007
 */
public class FollowerActionTest {

    @Test
    public void testFrameCreate() {
        Action a = new FollowerAction("test");
        Assert.assertNotNull(a);
    }

    @Test
    public void testActionCreateAndFire() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new FollowerAction("test").actionPerformed(null);
        JFrame f = JFrameOperator.waitJFrame(Bundle.getMessage("TitleFollower"), true, true);
        Assert.assertNotNull(f);
        JUnitUtil.dispose(f);
    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRouteManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
