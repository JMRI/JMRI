package jmri.jmrit.beantable;

import java.awt.GraphicsEnvironment;
import javax.swing.Action;
import javax.swing.JFrame;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Tests for classes in the jmri.jmrit.beantable package
 *
 * @author	Bob Jacobsen Copyright 2004
 */
public class MemoryTableActionTest {

    @Test
    public void testCreate() {
        Action a = new MemoryTableAction();
        Assert.assertNotNull(a);
    }

    @Test
    public void testExecute() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new MemoryTableAction().actionPerformed(null);
        JFrame f = JFrameOperator.waitJFrame(Bundle.getMessage("TitleMemoryTable"), true, true);
        Assert.assertNotNull("failed to find frame", f);
        f.dispose();
    }

    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
