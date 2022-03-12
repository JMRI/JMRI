package apps.gui3.dp3;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DecoderPro3ActionTest {

    @Test
    public void testCTor() {
        DecoderPro3Action t = new DecoderPro3Action("test",true);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testMakePanel(){
        DecoderPro3Action t = new DecoderPro3Action("test",true);
        Assert.assertThrows(IllegalArgumentException.class, () -> t.makePanel());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DecoderPro3ActionTest.class);

}
