package jmri.jmrix.loconet.sdfeditor;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LoadModifierEditorTest {

    @Test
    public void testCTor() {
        LoadModifierEditor t = new LoadModifierEditor(new jmri.jmrix.loconet.sdf.LoadModifier(1,2,3,4));
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

    // private final static Logger log = LoggerFactory.getLogger(LoadModifierEditorTest.class);

}
