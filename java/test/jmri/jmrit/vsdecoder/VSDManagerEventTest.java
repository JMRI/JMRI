package jmri.jmrit.vsdecoder;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class VSDManagerEventTest {

    @Test
    public void testCTor() {
        VSDecoderManager vsdm = new VSDecoderManager();
        VSDManagerEvent t = new VSDManagerEvent(vsdm);
        Assert.assertNotNull("exists",t);

        // this created an audio manager, clean that up
        InstanceManager.getDefault(jmri.AudioManager.class).cleanup();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(VSDManagerEventTest.class);

}
