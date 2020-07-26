package jmri.jmrix.loconet.sdfeditor;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class FourByteMacroEditorTest {

    @Test
    public void testCTor() {
        FourByteMacroEditor t = new FourByteMacroEditor(new jmri.jmrix.loconet.sdf.FourByteMacro(1,2,3,4));
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

    // private final static Logger log = LoggerFactory.getLogger(FourByteMacroEditorTest.class);

}
