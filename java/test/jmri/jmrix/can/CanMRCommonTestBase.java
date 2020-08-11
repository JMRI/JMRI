package jmri.jmrix.can;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Common tests for the jmri.jmrix.can.CanMessage and CanReply classes.
 *
 * @author Bob Jacobsen Copyright 2008, 2009
 */
public abstract class CanMRCommonTestBase extends jmri.jmrix.AbstractMessageTestBase {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    @AfterEach
    public void tearDown() {
        m = null;
        JUnitUtil.tearDown();
    }

}
