package jmri.jmrit.ctc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.atomic.AtomicInteger;

import jmri.*;
import jmri.jmrit.ctc.setup.CreateTestObjects;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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
    
    @Test
    public void testHandleNameModification() {
        
/*  Next, test in NBHTurnout the ability to dynmaically change the underlying turnout used
    WITHOUT affecting registered PropertyChangeListeners....
*/
//  Create and initialize standard JMRI turnouts:
        InstanceManager.getDefault(TurnoutManager.class).newTurnout("IT:TURNOUT", "TURNOUT");   // Create it if it doesn't exist.
        Turnout turnout2 = InstanceManager.getDefault(TurnoutManager.class).newTurnout("IT:TURNOUT2", "TURNOUT2");
        turnout2.setCommandedState(Turnout.CLOSED);

//  Our initial NBHTurnout, associate with "IT:TURNOUT":
        NBHTurnout turnoutNBH = new NBHTurnout("Module", "UserId", "Parameter", "TURNOUT", false);
        Assert.assertEquals(0, turnoutNBH.testingGetCountOfPropertyChangeListenersRegistered());     // Verify nothing registered yet.

//  Setup for the test:        
        PropertyChangeListener propertyChangeListener;
        AtomicInteger booleanContainer = new AtomicInteger(0);
        turnoutNBH.setCommandedState(Turnout.CLOSED);
        
//  NOTE: When setCommandedState is called, TWO PropertyChangeEvent's occur:
//        CommandedState and KnownState BOTH change!
        
        turnoutNBH.addPropertyChangeListener(propertyChangeListener = (PropertyChangeEvent e) -> { booleanContainer.incrementAndGet(); });
        Assert.assertEquals(1, turnoutNBH.testingGetCountOfPropertyChangeListenersRegistered());
        turnoutNBH.setCommandedState(Turnout.THROWN);
        Assert.assertEquals(2, booleanContainer.get());     // Make sure it works so far.
        
//  Simulate the user changing the turnout contained in the NBHTurnout to something else:        
        turnoutNBH.setHandleName("TURNOUT2");
        Assert.assertEquals(1, turnoutNBH.testingGetCountOfPropertyChangeListenersRegistered()); // We BETTER still be registered!

// Simulate as if SOMETHING OTHER THAN OUR CODE changed the state of turnout FLEETING2:
        turnout2.setCommandedState(Turnout.THROWN);
        
//  Did our PropertyChangeEvent happen?
//  This is what all this led up to, the REAL test!:        
        Assert.assertEquals(4, booleanContainer.get());
        
//  Clean up, and make sure our bookkeeping worked fine:
        turnoutNBH.removePropertyChangeListener(propertyChangeListener);
        Assert.assertEquals(0, turnoutNBH.testingGetCountOfPropertyChangeListenersRegistered());
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

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        // stop any BlockBossLogic threads created
        JUnitUtil.clearBlockBossLogic();

        jmri.util.JUnitUtil.tearDown();
    }
}
