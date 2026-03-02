package apps.gui3.dp3;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DecoderPro3ActionTest {

    @Test
    public void testCTor() {
        DecoderPro3Action t = new DecoderPro3Action("test",true);
        Assertions.assertNotNull(t, "exists");
    }

    @Test
    public void testMakePanel(){
        DecoderPro3Action t = new DecoderPro3Action("test",true);
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () -> t.makePanel());
        Assertions.assertNotNull(ex);
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
