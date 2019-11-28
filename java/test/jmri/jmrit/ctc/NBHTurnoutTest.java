package jmri.jmrit.ctc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.ctc.setup.CreateTestObjects;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.*;

/*
* Tests for the NBHTurnout Class
* @author  Dave Sand   Copyright (C) 2019
*/
public class NBHTurnoutTest {

    private PropertyChangeListener _testListener = null;

    @Test
    public void testGetsAndSets() {
        CreateTestObjects.createTurnout("IT93", "IT 93");

        // Use regular constructor
        NBHTurnout turnout93 = new NBHTurnout("Module", "UserId", "Parameter", "IT93", false);
        Assert.assertNotNull(turnout93);
        realBean(turnout93);

        // Use regular constructor with invalid name
        NBHTurnout turnout94 = new NBHTurnout("Module", "UserId", "Parameter", "IT94", false);
        Assert.assertNotNull(turnout94);
        nullBean(turnout94);

        JUnitAppender.suppressErrorMessage("Module, UserIdParameter, Turnout does not exist: IT94");
//         JUnitAppender.suppressErrorMessage("expected Sensor 1 not defined - IT93");
//         JUnitAppender.suppressErrorMessage("expected Sensor 2 not defined - IT93");
    }

// WARN  - expected Sensor 1 not defined - IT93 [main] jmri.implementation.AbstractTurnout.setInitialKnownStateFromFeedback()
// WARN  - expected Sensor 2 not defined - IT93 [main] jmri.implementation.AbstractTurnout.setInitialKnownStateFromFeedback()
// ERROR - Module, UserIdParameter, Turnout does not exist: IT94 [main] jmri.jmrit.ctc.CTCException.logError()

    public void nullBean(NBHTurnout turnout) {
        Turnout tbean = turnout.getBean();
        Assert.assertNull(tbean);

        int known = turnout.getKnownState();
        Assert.assertEquals(Turnout.CLOSED, known);

        turnout.setCommandedState(Turnout.THROWN);

        int feedback = turnout.getFeedbackMode();
        Assert.assertEquals(0, feedback);

        turnout.addPropertyChangeListener(_testListener = (PropertyChangeEvent e) -> {});
        turnout.removePropertyChangeListener(_testListener);
    }

    public void realBean(NBHTurnout turnout) {
        Turnout tbean = turnout.getBean();
        Assert.assertNotNull(tbean);

        int known = turnout.getKnownState();
        Assert.assertEquals(Turnout.CLOSED, known);

        turnout.setCommandedState(Turnout.THROWN);

        int feedback = turnout.getFeedbackMode();
        Assert.assertEquals(1, feedback);

        turnout.addPropertyChangeListener(_testListener = (PropertyChangeEvent e) -> {});
        turnout.removePropertyChangeListener(_testListener);
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        // stop any BlockBossLogic threads created
        JUnitUtil.clearBlockBossLogic();

        jmri.util.JUnitUtil.tearDown();
    }
}