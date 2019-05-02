package jmri;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.SignalGroup class
 *
 * @author	Egbert Broerse Copyright 2017
 */
public class SignalGroupTest {

    @Test
    public void testSetup() {
        // provide 2 turnouts:
        Turnout it1 = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        Turnout it2 = InstanceManager.turnoutManagerInstance().provideTurnout("IT2");
        Assert.assertEquals("it before", Turnout.UNKNOWN, it1.getCommandedState());
        Assert.assertEquals("it before", Turnout.UNKNOWN, it2.getCommandedState());
        // provide a single output signal head with IT1 as the output:
        jmri.implementation.SingleTurnoutSignalHead sh
                = new jmri.implementation.SingleTurnoutSignalHead("IH1",
                new jmri.NamedBeanHandle<Turnout>("IT1", it1),
                SignalHead.GREEN, SignalHead.DARK); // on state + off state
        Assert.assertNotNull("SignalHead is null!", sh);
        // provide a virtual signal mast:
        SignalMast sm = new jmri.implementation.VirtualSignalMast("IF$vsm:AAR-1946:CPL($0002)");
        Assert.assertNotNull("SignalMast is null!", sm);
        // provide a signal group:
        SignalGroup sg = InstanceManager.getDefault(jmri.SignalGroupManager.class).provideSignalGroup("IF12", "SignalGroup12");
        Assert.assertNotNull("SignalGroup is null!", sg);
        // add the head to the group:
        sg.addSignalHead(sh);
        // check value of OnState
        Assert.assertEquals("initial On state", SignalHead.GREEN, sg.getHeadOnState(sh));
        // set On state of IH1 from GREEN to LUNAR:
        sg.setHeadOnState(sh, SignalHead.LUNAR);
        Assert.assertEquals("new On state", SignalHead.LUNAR, sg.getHeadOnState(sh));
        // set IT1 to Closed to end Unknown state:
        it1.setCommandedState(Turnout.CLOSED); // Off = IH1 Dark
        it2.setCommandedState(Turnout.CLOSED); // set the control turnout Off
        // add IT2 as a control turnout to sh with conditional state Thrown:
        sg.setHeadAlignTurnout(sh, it2, Turnout.THROWN);
        Assert.assertEquals("group align turnout", "IT2", sg.getTurnoutNameByIndex(0, 0));
        sg.setSensorTurnoutOper(sh, false); // OR
        Assert.assertEquals("group logic oper", false, sg.getSensorTurnoutOperByIndex(0));
        // attach aspect Clear on mast sm to group
        sg.addSignalMastAspect("Clear"); // condition 1
        // set sm to Stop
        sm.setAspect("Stop");
        // Debug states
//        System.out.println("Before:");
//        System.out.println("IT1 =" + it1.getCommandedState());
//        System.out.println("IH1 =" + sh.getAppearanceName());
//        System.out.println("SM =" + sm.getAspect());
//        System.out.println("IT2 =" + it2.getCommandedState());
        // check state of member head sh
        Assert.assertEquals("sh before", Bundle.getMessage("SignalHeadStateDark"), sh.getAppearanceName());
        // now set conditional turnout
        it2.setCommandedState(Turnout.THROWN); // set the control turnout On = condition 2
        // set incuded aspect and check state of member head sh when conditional is met:
        sm.setAspect("Clear");
//        System.out.println("After:");
//        System.out.println("IT1 =" + it1.getCommandedState());
//        System.out.println("IH1 =" + sh.getAppearanceName());
//        System.out.println("SM =" + sm.getAspect());
//        System.out.println("IT2 =" + it2.getCommandedState());
        Assert.assertEquals("sh after", Bundle.getMessage("SignalHeadStateDark"), sh.getAppearanceName());
        // TODO would expect LUNAR instead, working on SignalGroup code
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
