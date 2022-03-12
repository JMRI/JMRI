package jmri.jmrit.roster;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class UpdateDecoderDefinitionActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriJFrame jf = new JmriJFrame("TestUpdateWindow");
        jmri.util.swing.WindowInterface wi = jf;
        UpdateDecoderDefinitionAction t = new UpdateDecoderDefinitionAction("test Update Decoder",wi);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(jf);
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

    // private final static Logger log = LoggerFactory.getLogger(UpdateDecoderDefinitionActionTest.class);

}
