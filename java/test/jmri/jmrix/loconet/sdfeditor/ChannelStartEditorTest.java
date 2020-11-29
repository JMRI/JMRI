package jmri.jmrix.loconet.sdfeditor;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ChannelStartEditorTest {

    @Test
    public void testCTor() {
        ChannelStartEditor t = new ChannelStartEditor(new jmri.jmrix.loconet.sdf.ChannelStart(1));
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

    // private final static Logger log = LoggerFactory.getLogger(ChannelStartEditorTest.class);

}
