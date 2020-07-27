package jmri.jmrit.display.switchboardEditor;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SwitchboardEditorActionTest {

    @Test
    public void testCTor() {
        SwitchboardEditorAction t = new SwitchboardEditorAction();
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

    // private final static Logger log = LoggerFactory.getLogger(SwitchboardEditorActionTest.class);

}
