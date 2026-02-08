package jmri.jmrit.entryexit;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfHeadless
public class AddEntryExitPairFrameTest extends jmri.util.JmriJFrameTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        frame = new AddEntryExitPairFrame();
    }

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AddEntryExitPairFrameTest.class);
}
