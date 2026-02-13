package jmri.jmrit.decoderdefn;

import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintDecoderListActionTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        JmriJFrame frame = new JmriJFrame("print decoder defn list test");
        PrintDecoderListAction t = new PrintDecoderListAction("test",frame,true);
        Assertions.assertNotNull(t, "exists");
        JUnitUtil.dispose(frame);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintDecoderListActionTest.class);

}
