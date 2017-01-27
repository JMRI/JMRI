package jmri;

import javax.swing.Action;
import javax.swing.JFrame;
import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.SignalGroup;
import jmri.Turnout;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.SignalGroup class
 *
 * @author	Egbert Broerse Copyright 2017
 */
public class SignalGroupTest {

    @Test
    public void testSetup() {
        //apps.tests.Log4JFixture.initLogging();
        // provide 2 turnouts:
        Turnout it1 = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        Turnout it2 = InstanceManager.turnoutManagerInstance().provideTurnout("IT2");
        Assert.assertEquals("it before", Turnout.UNKNOWN, it1.getCommandedState());
        Assert.assertEquals("it before", Turnout.UNKNOWN, it2.getCommandedState());
        // provide a single output signal head with IT1 as the output:
        jmri.implementation.SingleTurnoutSignalHead sh
                = new jmri.implementation.SingleTurnoutSignalHead("IH1",
                new jmri.NamedBeanHandle<Turnout>("IT1", it1),
                SignalHead.GREEN, SignalHead.DARK); // on state + off state (for now LUNAR from the beginning, stsh bug)
        Assert.assertNotNull("SignalHead is null!", sh);
        // provide a virtual signal mast:
        SignalMast sm = new jmri.implementation.VirtualSignalMast("IF$vsm:AAR-1946:CPL($0002)");
        Assert.assertNotNull("SignalMast is null!", sm);
        // provide a signal group:
        SignalGroup sg = InstanceManager.getDefault(jmri.SignalGroupManager.class).provideSignalGroup("R12", "SignalGroup12");
        Assert.assertNotNull("SignalGroup is null!", sg);
        // add the head to the group:
        sg.addSignalHead(sh);
        // set On state of IH1 from GREEN to LUNAR:
        sg.setHeadOnState(sh, SignalHead.LUNAR);
        Assert.assertEquals("new On state", SignalHead.LUNAR, sg.getHeadOnState(sh));
        // set IT1 to Closed to end Unknown state:
        it1.setCommandedState(Turnout.CLOSED); // Off = IH1 Dark
        // add IT2 as a control turnout to sh with conditional state Thrown and set IT2 to Closed/Off:
        sg.setHeadAlignTurnout(sh, it2, Turnout.THROWN);
        sg.setSensorTurnoutOper(sh, false); // OR
        it2.setCommandedState(Turnout.CLOSED); // set the control turnout Off
        // attach aspect Clear on mast sm to group
        sg.addSignalMastAspect("Clear"); // condition 1
        // set sm to Stop
        sm.setAspect("Stop");
        // Debug states
        //System.out.println("Before:");
        //System.out.println("IT1 =" + it1.getCommandedState());
        //System.out.println("IH1 =" + sh.getAppearanceName());
        //System.out.println("SM =" + sm.getAspect());
        //System.out.println("IT2 =" + it2.getCommandedState());
        // check state of member head sh
        Assert.assertEquals("sh before", Bundle.getMessage("SignalHeadStateDark"), sh.getAppearanceName());
        // check state of member head sh when conditional is met:
        sm.setAspect("Clear");
        it2.setCommandedState(Turnout.THROWN); // set the control turnout On = condition 2
        //System.out.println("After:");
        //System.out.println("IT1 =" + it1.getCommandedState());
        //System.out.println("IH1 =" + sh.getAppearanceName());
        //System.out.println("SM =" + sm.getAspect());
        //System.out.println("IT2 =" + it2.getCommandedState());
        Assert.assertEquals("sh after", Bundle.getMessage("SignalHeadStateDark"), sh.getAppearanceName());
        // TODO would expect LUNAR instead, working on SignalGroup code
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}
