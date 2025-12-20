package jmri.jmrit.ussctc;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;

/**
 * Tests for PhysicalBell class in the jmri.jmrit.ussctc package
 *
 * Can't do any particular tests with actual sounds because CI servers
 * don't support the jmri.jmrit.Sound class
 *
 * @author Bob Jacobsen Copyright 2021
 */
public class ComputerBellTest {

    @Test
    public void testNullConstruction() {
        Assertions.assertNotNull( new ComputerBell(null) );
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
