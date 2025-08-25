package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for the jmri.SignalGroup class
 *
 * @author Egbert Broerse Copyright 2017
 */
public class SignalGroupTest {

    @Test
    public void testSetup() {
        // provide 2 turnouts:
        Turnout it1 = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        Turnout it2 = InstanceManager.turnoutManagerInstance().provideTurnout("IT2");
        assertEquals( Turnout.UNKNOWN, it1.getCommandedState(), "it before");
        assertEquals( Turnout.UNKNOWN, it2.getCommandedState(), "it before");
        // provide a single output signal head with IT1 as the output:
        jmri.implementation.SingleTurnoutSignalHead sh
                = new jmri.implementation.SingleTurnoutSignalHead("IH1",
                new jmri.NamedBeanHandle<Turnout>("IT1", it1),
                SignalHead.GREEN, SignalHead.DARK); // on state + off state
        assertNotNull( sh, "SignalHead is null!");
        // provide a virtual signal mast:
        SignalMast sm = new jmri.implementation.VirtualSignalMast("IF$vsm:AAR-1946:CPL($0002)");
        assertNotNull( sm, "SignalMast is null!");
        // provide a signal group:
        SignalGroup sg = InstanceManager.getDefault(jmri.SignalGroupManager.class).provideSignalGroup("IF12", "SignalGroup12");
        assertNotNull( sg, "SignalGroup is null!");
        // add the head to the group:
        sg.addSignalHead(sh);
        // check value of OnState
        assertEquals( SignalHead.GREEN, sg.getHeadOnState(sh), "initial On state");
        // set On state of IH1 from GREEN to LUNAR:
        sg.setHeadOnState(sh, SignalHead.LUNAR);
        assertEquals( SignalHead.LUNAR, sg.getHeadOnState(sh), "new On state");
        // set IT1 to Closed to end Unknown state:
        it1.setCommandedState(Turnout.CLOSED); // Off = IH1 Dark
        it2.setCommandedState(Turnout.CLOSED); // set the control turnout Off
        // add IT2 as a control turnout to sh with conditional state Thrown:
        sg.setHeadAlignTurnout(sh, it2, Turnout.THROWN);
        assertEquals( "IT2", sg.getTurnoutNameByIndex(0, 0), "group align turnout");
        sg.setSensorTurnoutOper(sh, false); // OR
        assertFalse( sg.getSensorTurnoutOperByIndex(0), "group logic oper");
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
        assertEquals( Bundle.getMessage("SignalHeadStateDark"), sh.getAppearanceName(), "sh before");
        // now set conditional turnout
        it2.setCommandedState(Turnout.THROWN); // set the control turnout On = condition 2
        // set incuded aspect and check state of member head sh when conditional is met:
        sm.setAspect("Clear");
//        System.out.println("After:");
//        System.out.println("IT1 =" + it1.getCommandedState());
//        System.out.println("IH1 =" + sh.getAppearanceName());
//        System.out.println("SM =" + sm.getAspect());
//        System.out.println("IT2 =" + it2.getCommandedState());
        assertEquals( Bundle.getMessage("SignalHeadStateDark"), sh.getAppearanceName(), "sh after");
        // TODO would expect LUNAR instead, working on SignalGroup code
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initInternalTurnoutManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
