package jmri.jmrix.loconet.sdfeditor;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LabelMacroEditorTest {

    @Test
    public void testCTor() {
        LabelMacroEditor t = new LabelMacroEditor(new jmri.jmrix.loconet.sdf.LabelMacro("test"));
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

    // private final static Logger log = LoggerFactory.getLogger(LabelMacroEditorTest.class);

}
