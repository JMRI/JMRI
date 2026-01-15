package jmri;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutOperationManagerTest.class);

}
