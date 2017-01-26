package jmri.jmrit.beantable;

import javax.swing.Action;
import javax.swing.JFrame;
import jmri.SignalGroup;
//import jmri.util.JmriJFrame;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Tests for the jmri.jmrit.beantable.SignalGroupTableAction class
 *
 * @author	Egbert Broerse Copyright 2017
 */
public class SignalGroupTableActionTest {

    @Test
    public void testCreate() {
        Action a = new SignalGroupTableAction();
        Assert.assertNotNull(a);
    }

    @Test
    public void testInvoke() {
        new SignalGroupTableAction().actionPerformed(null);
        JFrame f = JFrameOperator.waitJFrame(Bundle.getMessage("TitleSignalGroupTable"), true, true);
        Assert.assertNotNull("found frame", f);
        f.dispose();
    }

    // TODO add test for Add... pane

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
