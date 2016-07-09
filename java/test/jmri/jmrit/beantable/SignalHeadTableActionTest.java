package jmri.jmrit.beantable;

import javax.swing.JFrame;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Turnout;
import jmri.implementation.DoubleTurnoutSignalHead;
import jmri.implementation.QuadOutputSignalHead;
import jmri.implementation.SE8cSignalHead;
import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrit.beantable.SignalHeadTableAction class
 *
 * @author	Bob Jacobsen Copyright 2004, 2007, 2008, 2009
 * @version	$Revision$
 */
public class SignalHeadTableActionTest extends jmri.util.SwingTestCase {

    public void testCreate() {
        new SignalHeadTableAction();
    }

    public void testInvoke() {
        // add a few signals and see if they exist
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                new DoubleTurnoutSignalHead("IH2", "double example 1-2",
                        new NamedBeanHandle<Turnout>("IT1", InstanceManager.turnoutManagerInstance().provideTurnout("IT1")),
                        new NamedBeanHandle<Turnout>("IT2", InstanceManager.turnoutManagerInstance().provideTurnout("IT2"))
                ));
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                new QuadOutputSignalHead("IH4", "quad example 11-14",
                        new NamedBeanHandle<Turnout>("IT11", InstanceManager.turnoutManagerInstance().provideTurnout("IT11")),
                        new NamedBeanHandle<Turnout>("IT12", InstanceManager.turnoutManagerInstance().provideTurnout("IT12")),
                        new NamedBeanHandle<Turnout>("IT13", InstanceManager.turnoutManagerInstance().provideTurnout("IT13")),
                        new NamedBeanHandle<Turnout>("IT14", InstanceManager.turnoutManagerInstance().provideTurnout("IT14"))
                ));

        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                new SE8cSignalHead(
                        new NamedBeanHandle<Turnout>("IT1", InstanceManager.turnoutManagerInstance().provideTurnout("IT21")),
                        new NamedBeanHandle<Turnout>("IT2", InstanceManager.turnoutManagerInstance().provideTurnout("IT22")),
                        "SE8c from handles")
        );

        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                new SE8cSignalHead(31, "SE8c from number")
        );

        new SignalHeadTableAction().actionPerformed(null);
        flushAWT();

        JFrame f = jmri.util.JmriJFrame.getFrame(Bundle.getMessage("TitleSignalTable"));
        Assert.assertTrue("found frame", f != null);
        f.dispose();
    }

    // from here down is testing infrastructure
    public SignalHeadTableActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SignalHeadTableActionTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SignalHeadTableActionTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSignalHeadManager();
    }

    protected void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
