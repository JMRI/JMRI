package jmri.jmrit.beantable.signalmast;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SignalMastRepeaterPanelTest {

    @Test
    public void testCTor() {
        SignalMastRepeaterPanel t = new SignalMastRepeaterPanel();
        Assertions.assertNotNull(t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SignalMastRepeaterPanelTest.class);

}
