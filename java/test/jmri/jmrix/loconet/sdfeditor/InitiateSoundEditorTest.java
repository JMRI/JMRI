package jmri.jmrix.loconet.sdfeditor;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class InitiateSoundEditorTest {

    @Test
    public void testCTor() {
        InitiateSoundEditor t = new InitiateSoundEditor(new jmri.jmrix.loconet.sdf.InitiateSound(1,2));
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

    // private final static Logger log = LoggerFactory.getLogger(InitiateSoundEditorTest.class);

}
