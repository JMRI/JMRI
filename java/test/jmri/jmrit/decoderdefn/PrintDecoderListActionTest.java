package jmri.jmrit.decoderdefn;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintDecoderListActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriJFrame frame = new JmriJFrame("print decoder defn list test");
        PrintDecoderListAction t = new PrintDecoderListAction("test",frame,true);
        Assert.assertNotNull("exists",t);
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
