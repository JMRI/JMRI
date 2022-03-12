package jmri.jmrix.loconet.sdfeditor;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DelaySoundEditorTest {

    @Test
    public void testCTor() {
        DelaySoundEditor t = new DelaySoundEditor(new jmri.jmrix.loconet.sdf.DelaySound(1,2));
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DelaySoundEditorTest.class);

}
