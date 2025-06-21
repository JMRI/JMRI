package jmri.jmrit.signalling;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class SignallingSourceFrameTest extends jmri.util.JmriJFrameTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        frame = new SignallingSourceFrame();
    }

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SignallingSourceFrameTest.class);
}
