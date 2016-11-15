package jmri.jmrit.ussctc;

import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import javax.swing.Action;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Tests for classes in the jmri.jmrit.ussctc.FollowerAction class
 *
 * @author	Bob Jacobsen Copyright 2003, 2007
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
        Frame f = JFrameOperator.waitJFrame("test", true, true);
        Assert.assertNotNull(f);
        f.dispose();
    }
}
