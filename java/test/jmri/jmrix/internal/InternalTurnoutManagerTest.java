package jmri.jmrix.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import jmri.NamedBean;
import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.internal.InternalTurnoutManager class.
 *
 * @author Bob Jacobsen Copyright 2016
 */
public class InternalTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "IT" + i;
    }

    @Override
    protected String getASystemNameWithNoPrefix() {
        return "My Turnout 6";
    }

    @Test
    public void testAsAbstractFactory() {

        // ask for a Turnout, and check type
        Turnout tl = l.newTurnout("IT21", "my name");

        Assert.assertNotNull(tl);

        // make sure loaded into tables
        Assert.assertNotNull(l.getBySystemName("IT21"));
        Assert.assertNotNull(l.getByUserName("my name"));
    }

    @Test
    public void testCaseMatters() {
        java.util.ArrayList<NamedBean> list = new  java.util.ArrayList<>();

        NamedBean tn1a = l.provide("name1");
        Assert.assertNotNull(tn1a);
        list.add(tn1a);

        // a test with specific system prefix attached (could get from this.getSystemName(1))
        NamedBean tn1b = l.provide("ITname1"); // meant to be same, note type-specific
        Assert.assertNotNull(tn1a);
        list.add(tn1b);
        Assert.assertEquals("tn1a and tn1b didn't match", tn1a, tn1b);

        // case is checked
        NamedBean tN1 = l.provide("NAME1");
        Assert.assertNotNull(tn1a);
        list.add(tN1);
        Assert.assertNotSame("tn1a doesn't match tN1, case not handled right", tn1a, tN1);

        // spaces fine, kept
        NamedBean tSpaceM  = l.provide("NAME 1");
        Assert.assertFalse("tSPaceM not unique", list.contains(tSpaceM));
        Assert.assertNotNull(tSpaceM);
        list.add(tSpaceM);

        NamedBean tSpaceMM = l.provide("NAME  1");
        Assert.assertFalse("tSpaceMM not unique", list.contains(tSpaceMM));
        Assert.assertNotNull(tSpaceMM);
        list.add(tSpaceMM);

        NamedBean tSpaceE  = l.provide("NAME 1 ");
        Assert.assertFalse("tSpaceE not unique", list.contains(tSpaceE));
        Assert.assertNotNull(tSpaceE);
        list.add(tSpaceE);

        NamedBean tSpaceEE  = l.provide("NAME 1  ");
        Assert.assertFalse("tSpaceEE not unique", list.contains(tSpaceEE));
        Assert.assertNotNull(tSpaceEE);
        list.add(tSpaceEE);

        NamedBean tSpaceLEE  = l.provide(" NAME 1  ");
        Assert.assertFalse("tSpaceLEE not unique", list.contains(tSpaceLEE));
        Assert.assertNotNull(tSpaceLEE);
        list.add(tSpaceLEE);

        NamedBean tSpaceLLEE  = l.provide("  NAME 1  ");
        Assert.assertFalse("tSpaceLLEE not unique", list.contains(tSpaceLLEE));
        Assert.assertNotNull(tSpaceLLEE);
        list.add(tSpaceLLEE);
    }

    @Test
    public void testFollowingTurnouts() {
        assumeThat(AbstractTurnout.DELAYED_FEEDBACK_INTERVAL)
                .as("Turnout delay less than JUnitUnit waitfor max delay")
                .isLessThan(JUnitUtil.WAITFOR_MAX_DELAY);
        Turnout t1 = l.provideTurnout("IT1");
        Turnout t2 = l.provideTurnout("IT2");

        assertThat(t1.getKnownState()).isEqualTo(Turnout.UNKNOWN);
        assertThat(t2.getKnownState()).isEqualTo(Turnout.UNKNOWN);
        assertThat(t1.getFeedbackMode()).isEqualTo(Turnout.DIRECT);
        assertThat(t2.getFeedbackMode()).isEqualTo(Turnout.DIRECT);
        assertThat(t1.getLeadingTurnout()).isNull();
        assertThat(t2.getLeadingTurnout()).isNull();
        assertThat(t1.isFollowingCommandedState()).isTrue();
        assertThat(t2.isFollowingCommandedState()).isTrue();

        t1.setCommandedState(Turnout.CLOSED);
        assertThat(t1.getKnownState()).isEqualTo(Turnout.CLOSED);
        assertThat(t2.getKnownState()).isEqualTo(Turnout.UNKNOWN);

        t2.setLeadingTurnout(t1);
        assertThat(t1.getKnownState()).isEqualTo(Turnout.CLOSED);
        assertThat(t2.getKnownState()).isEqualTo(Turnout.UNKNOWN);

        t1.setCommandedState(Turnout.THROWN);
        assertThat(t1.getKnownState()).isEqualTo(Turnout.THROWN);
        assertThat(t2.getKnownState()).isEqualTo(Turnout.THROWN);

        t1.setFeedbackMode(Turnout.DELAYED);
        t1.setCommandedState(Turnout.CLOSED);
        assertThat(t1.getKnownState()).isEqualTo(Turnout.INCONSISTENT);
        assertThat(t2.getKnownState()).isEqualTo(Turnout.INCONSISTENT);
        JUnitUtil.waitFor(() -> t1.getKnownState() == Turnout.CLOSED,"Turnout did not go closed");
        assertThat(t1.getKnownState()).isEqualTo(Turnout.CLOSED);
        assertThat(t2.getKnownState()).isEqualTo(Turnout.CLOSED);

        // verify no stack overflow when following in a circular pattern
        t1.setFeedbackMode(Turnout.DIRECT);
        t1.setLeadingTurnout(t2);
        assertThat(t1.getLeadingTurnout()).isEqualTo(t2);
        assertThat(t2.getLeadingTurnout()).isEqualTo(t1);
        // will throw stack overflow if not correctly implemented
        t1.setCommandedState(Turnout.THROWN);
        assertThat(t1.getKnownState()).isEqualTo(Turnout.THROWN);
        assertThat(t2.getKnownState()).isEqualTo(Turnout.THROWN);
        // will throw stack overflow if not correctly implemented
        t2.setCommandedState(Turnout.CLOSED);
        assertThat(t1.getKnownState()).isEqualTo(Turnout.CLOSED);
        assertThat(t2.getKnownState()).isEqualTo(Turnout.CLOSED);

        // verify
        t1.setFeedbackMode(Turnout.DELAYED);
        t1.setLeadingTurnout(null);
        t2.setFollowingCommandedState(false);
        assertThat(t1.getCommandedState()).isEqualTo(Turnout.CLOSED);
        assertThat(t2.getCommandedState()).isEqualTo(Turnout.CLOSED);
        t1.setCommandedState(Turnout.THROWN);
        assertThat(t1.getKnownState()).isEqualTo(Turnout.INCONSISTENT);
        assertThat(t2.getKnownState()).isEqualTo(Turnout.INCONSISTENT);
        JUnitUtil.waitFor(() -> t1.getKnownState() == Turnout.THROWN, "Turnout did not go thrown");
        assertThat(t1.getKnownState()).isEqualTo(Turnout.THROWN);
        assertThat(t2.getKnownState()).isEqualTo(Turnout.CLOSED);
    }

    @Test
    @Override
    public void testSetAndGetOutputInterval() {
        Assert.assertEquals("default outputInterval", 250, l.getOutputInterval());
        l.getMemo().setOutputInterval(21);
        Assert.assertEquals("new outputInterval in memo", 21, l.getMemo().getOutputInterval()); // set + get in memo
        Assert.assertEquals("new outputInterval via manager", 21, l.getOutputInterval()); // get via turnoutManager
        l.setOutputInterval(50); // interval set via ProxyTurnoutManager
        Assert.assertEquals("new outputInterval in memo", 50, l.getMemo().getOutputInterval()); // get directly from memo
        Assert.assertEquals("new outputInterval from manager", 50, l.getOutputInterval()); // get via turnoutManager
    }

    @Test
    @Override
    @Disabled("No manager-specific system name validation at present")
    public void testMakeSystemNameWithNoPrefixNotASystemName() {}

    @Test
    @Override
    @Disabled("No manager-specific system name validation at present")
    public void testMakeSystemNameWithPrefixNotASystemName() {}

    // from here down is testing infrastructure
    @Override
    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        // create and register the manager object
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        l = jmri.InstanceManager.turnoutManagerInstance();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
