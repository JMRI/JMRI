package jmri;

import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppInterfaceScaffold;
import jmri.jmrix.dccpp.DCCppTurnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TurnoutOperationManagerTest {

    @Test
    public void testCTor() {
        TurnoutOperationManager t = new TurnoutOperationManager();
        assertNotNull( t, "exists");
    }

    @Test
    public void testDefaultObjectCreation() {
        TurnoutOperationManager t = jmri.InstanceManager.getDefault(TurnoutOperationManager.class);
        assertNotNull( t, "exists");
    }

    @Test
    public void testToolTips() {
        TurnoutOperationManager t = new TurnoutOperationManager();
        assertNull(t.getTooltipForOperator("Not an operator", null));
        assertNull(t.getTooltipForOperator(null, null));
        assertNotNull(t.getTooltipForOperator(Bundle.getMessage("TurnoutOperationOff"), null));
    }

    @Test
    public void testDefaultTooltipNullWhenNoMatchingOperation() {
        // DIRECTPIN (512) has no matching TurnoutOperation; getTooltipForOperator
        // must return null rather than NPE when getMatchingOperationAlways returns null
        TurnoutOperationManager t = new TurnoutOperationManager();
        DCCppInterfaceScaffold xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        DCCppTurnout turnout = new DCCppTurnout("DCCPP", 42, xnis);
        turnout.setFeedbackMode(Turnout.DIRECTPIN);
        assertNull(t.getTooltipForOperator(Bundle.getMessage("TurnoutOperationDefault"), turnout));
        xnis.terminateThreads();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(TurnoutOperationManagerTest.class);

}
