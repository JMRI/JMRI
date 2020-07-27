package jmri.jmrix.loconet.sdfeditor;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SkemeStartEditorTest {

    @Test
    public void testCTor() {
        SkemeStartEditor t = new SkemeStartEditor(new jmri.jmrix.loconet.sdf.SkemeStart(1,2,3,4));
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

    // private final static Logger log = LoggerFactory.getLogger(SkemeStartEditorTest.class);

}
